package com.novaerp.stock.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransferRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Source warehouse is required")
    private Long sourceWarehouseId;

    @NotNull(message = "Target warehouse is required")
    private Long targetWarehouseId;

    @NotNull(message = "Transfer quantity is required")
    private BigDecimal quantity;

    private String notes;
}
