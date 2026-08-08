package com.novaerp.stock.repository;

import com.novaerp.stock.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @Query("SELECT sm FROM StockMovement sm JOIN FETCH sm.product p LEFT JOIN FETCH sm.sourceWarehouse sw LEFT JOIN FETCH sm.targetWarehouse tw WHERE sm.product.id = :productId ORDER BY sm.createdAt DESC")
    List<StockMovement> findByProductIdOrderByCreatedAtDesc(@Param("productId") Long productId);

    @Query(value = "SELECT sm FROM StockMovement sm JOIN FETCH sm.product p LEFT JOIN FETCH sm.sourceWarehouse sw LEFT JOIN FETCH sm.targetWarehouse tw",
           countQuery = "SELECT COUNT(sm) FROM StockMovement sm")
    Page<StockMovement> findAllWithDetails(Pageable pageable);
}
