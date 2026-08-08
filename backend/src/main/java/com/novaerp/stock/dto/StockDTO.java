package com.novaerp.stock.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.novaerp.stock.entity.Stock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockDTO {

    private Long id;
    private Long productId;

    @JsonProperty("produitNom")
    @JsonAlias({"produitNom", "productName"})
    private String productName;

    @JsonProperty("reference")
    @JsonAlias({"reference", "sku"})
    private String sku;

    private Long warehouseId;
    private String warehouseName;

    @JsonProperty("quantiteStock")
    @JsonAlias({"quantiteStock", "quantityOnHand"})
    private BigDecimal quantityOnHand;

    private BigDecimal quantityAllocated;
    private BigDecimal quantityAvailable;

    @JsonProperty("seuilMinimum")
    @JsonAlias({"seuilMinimum", "minStockLevel"})
    private BigDecimal minStockLevel;

    private boolean isLowStock;

    public static StockDTO fromEntity(Stock stock) {
        boolean lowStock = stock.getProduct() != null &&
                stock.getQuantityOnHand().compareTo(stock.getProduct().getMinStockLevel()) <= 0;

        return StockDTO.builder()
                .id(stock.getId())
                .productId(stock.getProduct().getId())
                .productName(stock.getProduct().getName())
                .sku(stock.getProduct().getSku())
                .warehouseId(stock.getWarehouse().getId())
                .warehouseName(stock.getWarehouse().getName())
                .quantityOnHand(stock.getQuantityOnHand())
                .quantityAllocated(stock.getQuantityAllocated())
                .quantityAvailable(stock.getQuantityAvailable())
                .minStockLevel(stock.getProduct().getMinStockLevel())
                .isLowStock(lowStock)
                .build();
    }
}
