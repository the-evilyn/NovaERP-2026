package com.novaerp.ai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiPredictionDTO {

    @JsonProperty("produitId")
    private Long produitId;

    @JsonProperty("produitNom")
    private String produitNom;

    @JsonProperty("sku")
    private String sku;

    @JsonProperty("stockActuel")
    @Builder.Default
    private BigDecimal stockActuel = BigDecimal.ZERO;

    @JsonProperty("consommationMoyenne")
    @Builder.Default
    private BigDecimal consommationMoyenne = BigDecimal.ZERO;

    @JsonProperty("joursRestants")
    @Builder.Default
    private Integer joursRestants = 0;

    @JsonProperty("dateRupturePrevue")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateRupturePrevue;

    @JsonProperty("recommandation")
    private String recommandation; // 'COMMANDER_URGENT' | 'COMMANDER_BIENTOT' | 'STOCK_OPTIMAL' | 'SURSTOCK'

    @JsonProperty("quantiteRecommandee")
    @Builder.Default
    private BigDecimal quantiteRecommandee = BigDecimal.ZERO;

    @JsonProperty("fournisseurSuggere")
    private SuggestedSupplierDTO fournisseurSuggere;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuggestedSupplierDTO {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("nom")
        private String nom;

        @JsonProperty("prixUnitaire")
        private BigDecimal prixUnitaire;

        @JsonProperty("delaiLivraisonJours")
        private Integer delaiLivraisonJours;
    }
}
