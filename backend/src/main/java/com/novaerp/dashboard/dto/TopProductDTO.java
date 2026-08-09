package com.novaerp.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductDTO {

    private Long id;

    @JsonProperty("productId")
    public Long getProductId() {
        return id;
    }

    public void setProductId(Long productId) {
        this.id = productId;
    }

    @JsonProperty("nom")
    private String nom;

    @JsonProperty("sku")
    private String sku;

    @JsonProperty("quantiteVendue")
    @Builder.Default
    private BigDecimal quantiteVendue = BigDecimal.ZERO;

    @JsonProperty("chiffreAffaires")
    @Builder.Default
    private BigDecimal chiffreAffaires = BigDecimal.ZERO;
}
