package com.novaerp.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.novaerp.product.dto.ProductDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {

    @JsonProperty("chiffreAffaires")
    @Builder.Default
    private BigDecimal chiffreAffaires = BigDecimal.ZERO;

    @JsonProperty("benefice")
    @Builder.Default
    private BigDecimal benefice = BigDecimal.ZERO;

    @JsonProperty("topClients")
    @Builder.Default
    private List<TopClientDTO> topClients = new ArrayList<>();

    @JsonProperty("topProduits")
    @Builder.Default
    private List<TopProductDTO> topProduits = new ArrayList<>();

    @JsonProperty("produitsStockFaible")
    @Builder.Default
    private List<ProductDTO> produitsStockFaible = new ArrayList<>();

    @JsonProperty("produitsDormants")
    @Builder.Default
    private List<ProductDTO> produitsDormants = new ArrayList<>();

    // Enterprise KPIs for extended analytics
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
