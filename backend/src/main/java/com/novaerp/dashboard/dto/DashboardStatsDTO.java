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
public class DashboardStatsDTO {

    @JsonProperty("totalVentes")
    @Builder.Default
    private BigDecimal totalVentes = BigDecimal.ZERO;

    @JsonProperty("totalAchats")
    @Builder.Default
    private BigDecimal totalAchats = BigDecimal.ZERO;

    @JsonProperty("valeurStock")
    @Builder.Default
    private BigDecimal valeurStock = BigDecimal.ZERO;

    @JsonProperty("totalClients")
    @Builder.Default
    private Long totalClients = 0L;

    @JsonProperty("totalFournisseurs")
    @Builder.Default
    private Long totalFournisseurs = 0L;

    @JsonProperty("facturesEnAttente")
    @Builder.Default
    private Long facturesEnAttente = 0L;

    @JsonProperty("alertesStock")
    @Builder.Default
    private Long alertesStock = 0L;

    @JsonProperty("chiffreAffairesMois")
    @Builder.Default
    private BigDecimal chiffreAffairesMois = BigDecimal.ZERO;

    @JsonProperty("evolutionVentes")
    @Builder.Default
    private Double evolutionVentes = 0.0;

    @JsonProperty("evolutionAchats")
    @Builder.Default
    private Double evolutionAchats = 0.0;
}
