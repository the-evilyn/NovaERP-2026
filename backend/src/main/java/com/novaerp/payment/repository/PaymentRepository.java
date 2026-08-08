package com.novaerp.payment.repository;

import com.novaerp.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentNumber(String paymentNumber);

    @Query("SELECT p FROM Payment p LEFT JOIN p.client c LEFT JOIN p.invoice inv WHERE " +
           "LOWER(p.paymentNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(COALESCE(c.name, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(COALESCE(inv.invoiceNumber, '')) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Payment> searchPayments(@Param("search") String search, Pageable pageable);

    List<Payment> findByInvoiceId(Long invoiceId);
    List<Payment> findByClientId(Long clientId);
}
