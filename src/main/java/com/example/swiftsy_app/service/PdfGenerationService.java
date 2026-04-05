package com.example.swiftsy_app.service;

import com.example.swiftsy_app.domain.Invoice;
import com.example.swiftsy_app.domain.InvoiceItem;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class PdfGenerationService {

    public byte[] generateInvoicePdf(Invoice invoice) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Header Font Options
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font boldCol = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

            // Company Header
            Paragraph header = new Paragraph("SWIFTSY FREIGHT PVT LTD", titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph address = new Paragraph("Plot - 56, Shop No. 1A, Monarch Plaza Ground Floor, Sector 11, CBD Belapur\nNavi Mumbai, Maharashtra 400614", bodyFont);
            address.setAlignment(Element.ALIGN_CENTER);
            document.add(address);
            
            document.add(new Paragraph("\n"));
            
            Paragraph invoiceIdText = new Paragraph("INVOICE", subTitleFont);
            invoiceIdText.setAlignment(Element.ALIGN_CENTER);
            document.add(invoiceIdText);
            document.add(new Paragraph("\n"));

            // Info Table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingBefore(10f);
            
            // Customer Details Cell
            PdfPCell customerCell = new PdfPCell();
            customerCell.setBorder(Rectangle.NO_BORDER);
            customerCell.addElement(new Paragraph("Billed To:", boldCol));
            customerCell.addElement(new Paragraph(invoice.getCustomer().getName(), bodyFont));
            customerCell.addElement(new Paragraph(invoice.getCustomer().getAddress(), bodyFont));
            customerCell.addElement(new Paragraph("GST: " + invoice.getCustomer().getGstNumber(), bodyFont));
            infoTable.addCell(customerCell);

            // Invoice & Shipment Details Cell
            PdfPCell detailsCell = new PdfPCell();
            detailsCell.setBorder(Rectangle.NO_BORDER);
            detailsCell.addElement(new Paragraph("Invoice Number: " + invoice.getInvoiceNumber(), bodyFont));
            detailsCell.addElement(new Paragraph("Invoice Date: " + invoice.getDate(), bodyFont));
            detailsCell.addElement(new Paragraph("BL Number: " + invoice.getBlNumber(), bodyFont));
            detailsCell.addElement(new Paragraph("Container: " + invoice.getContainerNumber(), bodyFont));
            detailsCell.addElement(new Paragraph("Origin: " + invoice.getOriginPort() + "  ->  Dest: " + invoice.getDestinationPort(), bodyFont));
            infoTable.addCell(detailsCell);
            
            document.add(infoTable);
            document.add(new Paragraph("\n\n"));

            // Charges Table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 1f});

            PdfPCell h1 = new PdfPCell(new Phrase("Description", boldCol));
            h1.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
            h1.setPadding(5);
            PdfPCell h2 = new PdfPCell(new Phrase("Amount (INR)", boldCol));
            h2.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
            h2.setPadding(5);
            table.addCell(h1);
            table.addCell(h2);

            for (InvoiceItem item : invoice.getItems()) {
                PdfPCell c1 = new PdfPCell(new Phrase(item.getDescription(), bodyFont));
                c1.setPadding(5);
                table.addCell(c1);

                PdfPCell c2 = new PdfPCell(new Phrase(String.format("%.2f", item.getAmount()), bodyFont));
                c2.setPadding(5);
                c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(c2);
            }

            // Totals
            PdfPCell subtotalCell = new PdfPCell(new Phrase("Subtotal", boldCol));
            subtotalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            subtotalCell.setPadding(5);
            table.addCell(subtotalCell);

            PdfPCell subtotalVal = new PdfPCell(new Phrase(String.format("%.2f", invoice.getSubtotal()), bodyFont));
            subtotalVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            subtotalVal.setPadding(5);
            table.addCell(subtotalVal);

            PdfPCell gstCell = new PdfPCell(new Phrase("GST (" + invoice.getGstPercentage() + "%)", boldCol));
            gstCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            gstCell.setPadding(5);
            table.addCell(gstCell);

            PdfPCell gstVal = new PdfPCell(new Phrase(String.format("%.2f", invoice.getGstAmount()), bodyFont));
            gstVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            gstVal.setPadding(5);
            table.addCell(gstVal);

            PdfPCell gTotalCell = new PdfPCell(new Phrase("Grand Total", titleFont));
            gTotalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            gTotalCell.setPadding(5);
            table.addCell(gTotalCell);

            PdfPCell gTotalVal = new PdfPCell(new Phrase(String.format("%.2f", invoice.getGrandTotal()), titleFont));
            gTotalVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            gTotalVal.setPadding(5);
            table.addCell(gTotalVal);

            document.add(table);

            document.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }
}
