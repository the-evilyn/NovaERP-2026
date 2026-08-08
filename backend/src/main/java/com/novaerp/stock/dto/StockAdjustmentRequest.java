package com.novaerp.stock.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class StockAdjustmentRequest {

    @NotNull(message = "Product ID is required")
    @JsonProperty("produitId")
    @JsonAlias({"produitId", "productId"})
    private Long productId;

    private Long warehouseId;

    @NotNull(message = "Quantity is required")
    @JsonProperty("quantite")
    @JsonAlias({"quantite", "quantity"})
    private BigDecimal quantity;

    @JsonProperty("type")
    @Builder.Default
    private String type = "AJUSTEMENT"; // ENTREE, SORTIE, AJUSTEMENT

    @JsonProperty("motif")
    @JsonAlias({"motif", "reason", "notes"})
    private String motif;
}
