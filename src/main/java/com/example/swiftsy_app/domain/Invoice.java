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

    // Items
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items = new ArrayList<>();

    // Tax
    private double gstPercentage;

    // Totals
    private double subtotal;
    private double gstAmount;
    private double grandTotal;
}
