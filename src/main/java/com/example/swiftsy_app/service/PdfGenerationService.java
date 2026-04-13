package com.example.swiftsy_app.service;

import com.example.swiftsy_app.domain.Invoice;
import com.example.swiftsy_app.domain.InvoiceItem;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PdfGenerationService {

    private final TemplateEngine templateEngine;

    public PdfGenerationService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generateInvoicePdf(Invoice invoice) {
        Context context = new Context();

        // 1. Prepare Company Details
        Map<String, Object> company = new HashMap<>();
        company.put("name", "SWIFTSY FREIGHT PVT. LTD.");
        company.put("gstin", "27AAXCS0569G2ZQ");
        company.put("pan", "AAXCS0569G");
        company.put("cin", "U7499MH2016PTC280970");
        company.put("addressLine1", "Shop No. 1A, Monarch Plaza Ground Floor");
        company.put("addressLine2", "Sec - 11, Plot - 56, CBD Belapur, Navi Mumbai - 400614.");
        company.put("email", "info@swiftsy.in");
        company.put("phone", "022 4006 7629");
        context.setVariable("company", company);

        // 2. Prepare Customer Details (Advanced Template expects 'client')
        Map<String, Object> client = new HashMap<>();
        if (invoice.getCustomer() != null) {
            client.put("name", invoice.getCustomer().getName() != null ? invoice.getCustomer().getName() : "");
            client.put("addressLine1", invoice.getCustomer().getAddress() != null ? invoice.getCustomer().getAddress() : "");
            client.put("addressLine2", null); // DB only has one address right now
            client.put("gstin", invoice.getCustomer().getGstNumber() != null ? invoice.getCustomer().getGstNumber() : "");
        }
        context.setVariable("client", client);

        // 3. Prepare Invoice Meta
        Map<String, Object> invoiceMeta = new HashMap<>();
        invoiceMeta.put("number", invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "");
        invoiceMeta.put("date", invoice.getDate() != null ? invoice.getDate() : "");
        invoiceMeta.put("jobNumber", invoice.getJobNumber() != null ? invoice.getJobNumber() : "");
        invoiceMeta.put("shippingBillRef", invoice.getShippingBillRef() != null ? invoice.getShippingBillRef() : "");
        invoiceMeta.put("title", invoice.getTitle() != null && !invoice.getTitle().isEmpty() ? invoice.getTitle() : "TAX INVOICE");
        invoiceMeta.put("exchangeRateNote", invoice.getExchangeRateNote() != null ? invoice.getExchangeRateNote() : "");
        
        Map<String, Object> totalsMap = new HashMap<>();
        double grandTotalVal = invoice.getGrandTotal() != null ? invoice.getGrandTotal() : 0.0;
        double totalGstVal = invoice.getGstAmount();
        totalsMap.put("amountInr", invoice.getSubtotal());
        totalsMap.put("totalSgst", totalGstVal / 2.0);
        totalsMap.put("totalCgst", totalGstVal / 2.0);
        totalsMap.put("grandTotal", grandTotalVal);
        invoiceMeta.put("totals", totalsMap);
        invoiceMeta.put("amountInWords", convertToIndianCurrencyWords((long) grandTotalVal));

        List<String> terms = new ArrayList<>();
        terms.add("All cheques/DDs to be drawn in favor of SWIFTSY FREIGHT PVT LTD");
        terms.add("Interest @ 24% p.a. will be charged if payment is delayed.");
        terms.add("Subject to Navi Mumbai Jurisdiction");
        invoiceMeta.put("terms", terms);
        context.setVariable("invoice", invoiceMeta);

        // 4. Prepare Shipment Details
        Map<String, Object> shipment = new HashMap<>();
        shipment.put("portOfLoading", invoice.getOriginPort() != null ? invoice.getOriginPort() : "");
        shipment.put("portOfDischarge", invoice.getDestinationPort() != null ? invoice.getDestinationPort() : "");
        shipment.put("containerNumber", invoice.getContainerNumber() != null ? invoice.getContainerNumber() : "");
        shipment.put("numberOfPackages", invoice.getNumberOfPackages() != null ? invoice.getNumberOfPackages() : "-");
        shipment.put("weight", invoice.getWeight() != null ? invoice.getWeight() : "-");
        shipment.put("measurement", invoice.getMeasurement() != null ? invoice.getMeasurement() : "");
        shipment.put("exchangeRate", invoice.getExchangeRate() != null ? invoice.getExchangeRate() : 1.0);
        context.setVariable("shipment", shipment);

        // 5. Prepare Table Items
        List<Map<String, Object>> items = new ArrayList<>();
        if (invoice.getItems() != null) {
            for (InvoiceItem item : invoice.getItems()) {
                Map<String, Object> i = new HashMap<>();
                i.put("description", item.getDescription());
                i.put("currency", "INR");
                i.put("rate", item.getAmount());
                i.put("quantity", 1);
                i.put("amountInr", item.getAmount());
                i.put("gstPercent", invoice.getGstPercentage());
                
                double amount = item.getAmount();
                double gstPart = (amount * invoice.getGstPercentage()) / 100.0;
                double sgst = gstPart / 2.0;
                double cgst = gstPart / 2.0;
                
                i.put("sgst", sgst);
                i.put("cgst", cgst);
                i.put("total", amount + gstPart);
                
                items.add(i);
            }
        }
        invoiceMeta.put("items", items);

        // 6. Bank Details (Advanced Template uses 'bankName' and omits 'total' root obj)
        Map<String, Object> bank = new HashMap<>();
        bank.put("bankName", "HDFC BANK"); 
        bank.put("accountNumber", "50200021487596");
        bank.put("accountName", "SWIFTSY FREIGHT PVT LTD");
        bank.put("ifscCode", "HDFC0000123");
        bank.put("swiftCode", null);
        context.setVariable("bank", bank);

        // 8. Logo Base64 Encode
        try {
            ClassPathResource resource = new ClassPathResource("static/assets/images/main-logo.png");
            try (InputStream is = resource.getInputStream()) {
                byte[] logoBytes = is.readAllBytes();
                String base64Image = Base64.getEncoder().encodeToString(logoBytes);
                context.setVariable("logoBase64", base64Image);
            }
        } catch (Exception e) {
            context.setVariable("logoBase64", ""); // fallback
        }

        // Process Thymeleaf -> HTML5 string
        String html = templateEngine.process("invoice-template", context);

        // Parse with Jsoup to strict XHTML to prevent SAXParseException with unclosed tags!
        Document jsoupDoc = Jsoup.parse(html);
        jsoupDoc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        String xhtml = jsoupDoc.html();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            
            // Provide the strict XHTML string to Flying Saucer
            renderer.setDocumentFromString(xhtml);
            renderer.layout();
            renderer.createPDF(outputStream);
            
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF from HTML Template", e);
        }
    }

    private String convertToIndianCurrencyWords(long number) {
        if (number == 0) return "Zero Rupees Only";
        
        String words = "";
        
        words += convertGroup((int)(number / 10000000), "Crore ");
        number %= 10000000;
        
        words += convertGroup((int)(number / 100000), "Lakh ");
        number %= 100000;
        
        words += convertGroup((int)(number / 1000), "Thousand ");
        number %= 1000;
        
        words += convertGroup((int)(number / 100), "Hundred ");
        number %= 100;

        if (number > 0) {
            words += convertGroup((int)number, "");
        }
        
        return "Rupees " + words.trim() + " Only";
    }

    private String convertGroup(int n, String suffix) {
        String[] units = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
        String[] tens = {"", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};
        
        String str = "";
        if (n > 19) {
            str += tens[n / 10] + " " + units[n % 10];
        } else {
            str += units[n];
        }
        
        if (n != 0) {
            str += " " + suffix;
        }
        return str.trim() + " ";
    }
}
