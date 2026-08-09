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
public class TopClientDTO {

    @JsonProperty("clientId")
    private Long clientId;

    @JsonProperty("nom")
    private String nom;

    @JsonProperty("total")
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;
}
