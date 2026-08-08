package com.novaerp.purchase.repository;

import com.novaerp.purchase.entity.PurchaseOrder;
import com.novaerp.purchase.entity.PurchaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Optional<PurchaseOrder> findByOrderNumber(String orderNumber);
    boolean existsByOrderNumber(String orderNumber);

    @Query("SELECT po FROM PurchaseOrder po JOIN FETCH po.supplier s WHERE " +
           "LOWER(po.orderNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<PurchaseOrder> searchPurchases(@Param("search") String search, Pageable pageable);

    Page<PurchaseOrder> findByStatus(PurchaseStatus status, Pageable pageable);
}
