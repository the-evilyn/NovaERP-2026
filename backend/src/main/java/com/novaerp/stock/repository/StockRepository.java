package com.novaerp.stock.repository;

import com.novaerp.stock.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByProductIdAndWarehouseId(Long productId, Long warehouseId);
    List<Stock> findByProductId(Long productId);

    @Query("SELECT COALESCE(SUM(s.quantityOnHand), 0) FROM Stock s WHERE s.product.id = :productId")
    BigDecimal getTotalQuantityOnHandByProductId(@Param("productId") Long productId);

    @Query("SELECT s FROM Stock s JOIN FETCH s.product p WHERE s.quantityOnHand <= p.minStockLevel")
    List<Stock> findLowStockItems();
}
