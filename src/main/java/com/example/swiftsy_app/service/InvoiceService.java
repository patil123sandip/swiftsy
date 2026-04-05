package com.example.swiftsy_app.service;

import com.example.swiftsy_app.domain.Invoice;
import com.example.swiftsy_app.domain.InvoiceItem;
import com.example.swiftsy_app.repository.CustomerRepository;
import com.example.swiftsy_app.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, CustomerRepository customerRepository) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Optional<Invoice> getInvoiceById(Long id) {
        return invoiceRepository.findById(id);
    }

    public Invoice createInvoice(Invoice invoice) {
        // Auto generate Invoice Number
        Long maxId = invoiceRepository.getMaxId();
        invoice.setInvoiceNumber("SW-INV-" + (1000 + maxId + 1));
        
        if (invoice.getDate() == null) {
            invoice.setDate(LocalDate.now());
        }

        // Link items
        if (invoice.getItems() != null) {
            for (InvoiceItem item : invoice.getItems()) {
                item.setInvoice(invoice);
            }
        }

        // Calculate Totals
        calculateTotals(invoice);

        return invoiceRepository.save(invoice);
    }
    
    public Invoice updateInvoice(Long id, Invoice updatedInvoice) {
        return invoiceRepository.findById(id).map(existing -> {
            existing.setCustomer(updatedInvoice.getCustomer());
            existing.setBlNumber(updatedInvoice.getBlNumber());
            existing.setContainerNumber(updatedInvoice.getContainerNumber());
            existing.setOriginPort(updatedInvoice.getOriginPort());
            existing.setDestinationPort(updatedInvoice.getDestinationPort());
            existing.setShipmentDate(updatedInvoice.getShipmentDate());
            existing.setGstPercentage(updatedInvoice.getGstPercentage());
            
            existing.getItems().clear();
            if (updatedInvoice.getItems() != null) {
                for (InvoiceItem item : updatedInvoice.getItems()) {
                    item.setInvoice(existing);
                    existing.getItems().add(item);
                }
            }
            
            calculateTotals(existing);
            return invoiceRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Invoice not found"));
    }

    public void deleteInvoice(Long id) {
        invoiceRepository.deleteById(id);
    }

    private void calculateTotals(Invoice invoice) {
        double subtotal = 0;
        if (invoice.getItems() != null) {
            for (InvoiceItem item : invoice.getItems()) {
                subtotal += item.getAmount();
            }
        }
        invoice.setSubtotal(subtotal);
        double gstAmount = (subtotal * invoice.getGstPercentage()) / 100.0;
        invoice.setGstAmount(gstAmount);
        invoice.setGrandTotal(subtotal + gstAmount);
    }
}
