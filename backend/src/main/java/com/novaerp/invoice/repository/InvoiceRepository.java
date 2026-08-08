package com.novaerp.invoice.repository;

import com.novaerp.invoice.entity.Invoice;
import com.novaerp.invoice.entity.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    boolean existsByInvoiceNumber(String invoiceNumber);

    @Query("SELECT inv FROM Invoice inv JOIN FETCH inv.client c WHERE " +
           "LOWER(inv.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Invoice> searchInvoices(@Param("search") String search, Pageable pageable);

    List<Invoice> findByClientId(Long clientId);
    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);
}
