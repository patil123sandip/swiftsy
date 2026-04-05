package com.example.swiftsy_app.repository;

import com.example.swiftsy_app.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    @Query("SELECT coalesce(max(i.id), 0) FROM Invoice i")
    Long getMaxId();
}
