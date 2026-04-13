package com.example.swiftsy_app.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String invoiceNumber;
    
    private LocalDate date;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // Shipment Details
    private String blNumber;
    private String containerNumber;
    private String originPort;
    private String destinationPort;
    private LocalDate shipmentDate;

    // Advanced Invoice Meta
    private String jobNumber;
    private String shippingBillRef;
    private String title;
    private String exchangeRateNote;

    // Advanced Shipment Metrics
    private String numberOfPackages;
    private String weight;
    private String measurement;
    private Double exchangeRate;

    // Items
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items = new ArrayList<>();

    // Tax
    private double gstPercentage;

    // Totals
    private double subtotal;
    private double gstAmount;
    private Double grandTotal;
}
