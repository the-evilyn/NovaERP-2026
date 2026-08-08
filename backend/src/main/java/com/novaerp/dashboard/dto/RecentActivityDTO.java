package com.novaerp.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityDTO {

    private Long id;

    @JsonProperty("type")
    private String type; // 'VENTE' | 'ACHAT' | 'STOCK' | 'PAIEMENT' | 'CLIENT'

    @JsonProperty("description")
    private String description;

    @JsonProperty("date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;

    @JsonProperty("montant")
    private BigDecimal montant;

    @JsonProperty("statut")
    private String statut;
}
