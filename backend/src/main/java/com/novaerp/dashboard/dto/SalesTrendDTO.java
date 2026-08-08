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
public class SalesTrendDTO {

    @JsonProperty("mois")
    private String mois;

    @JsonProperty("ventes")
    @Builder.Default
    private BigDecimal ventes = BigDecimal.ZERO;

    @JsonProperty("achats")
    @Builder.Default
    private BigDecimal achats = BigDecimal.ZERO;
}
