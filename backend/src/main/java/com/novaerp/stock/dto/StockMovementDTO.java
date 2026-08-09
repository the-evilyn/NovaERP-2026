package com.novaerp.stock.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.novaerp.stock.entity.StockMovement;
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
public class StockMovementDTO {

    private Long id;

    @JsonProperty("productId")
    @JsonAlias({"productId", "produitId"})
    private Long productId;

    @JsonProperty("produitId")
    public Long getProduitId() {
        return productId;
    }

    public void setProduitId(Long produitId) {
        this.productId = produitId;
    }

    @JsonProperty("productNom")
    @JsonAlias({"productNom", "produitNom", "productName"})
    private String productNom;

    @JsonProperty("produitNom")
    public String getProduitNom() {
        return productNom;
    }

    public void setProduitNom(String produitNom) {
        this.productNom = produitNom;
    }

    @JsonProperty("productName")
    public String getProductName() {
        return productNom;
    }

    public void setProductName(String productName) {
        this.productNom = productName;
    }

    @JsonProperty("type")
    private String type; // ENTREE, SORTIE, AJUSTEMENT, TRANSFERT, RETOUR

    @JsonProperty("quantite")
    @JsonAlias({"quantite", "quantity"})
    private BigDecimal quantite;

    @JsonProperty("quantity")
    public BigDecimal getQuantity() {
        return quantite;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantite = quantity;
    }

    @JsonProperty("motif")
    @JsonAlias({"motif", "reason", "notes"})
    private String motif;

    @JsonProperty("stockApres")
    @Builder.Default
    private BigDecimal stockApres = BigDecimal.ZERO;

    @JsonProperty("userId")
    @Builder.Default
    private Long userId = 1L;

    @JsonProperty("username")
    @Builder.Default
    private String username = "admin";

    @JsonProperty("date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;

    private Long sourceWarehouseId;
    private String sourceWarehouseName;
    private Long targetWarehouseId;
    private String targetWarehouseName;
    private String referenceType;
    private String referenceId;
    private BigDecimal unitCost;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static StockMovementDTO fromEntity(StockMovement sm) {
        String frontendType = switch (sm.getMovementType()) {
            case IN_PURCHASE -> "ENTREE";
            case OUT_SALE -> "SORTIE";
            case ADJUSTMENT_IN, ADJUSTMENT_OUT -> "AJUSTEMENT";
            case TRANSFER -> "TRANSFERT";
            case RETURN -> "RETOUR";
        };

        String motif = sm.getNotes() != null ? sm.getNotes() : (sm.getReferenceType() != null ? sm.getReferenceType() : "CORRECTION");

        return StockMovementDTO.builder()
                .id(sm.getId())
                .productId(sm.getProduct().getId())
                .productNom(sm.getProduct().getName())
                .type(frontendType)
                .quantite(sm.getQuantity())
                .motif(motif)
                .stockApres(sm.getQuantity())
                .userId(1L)
                .username(sm.getCreatedBy() != null ? sm.getCreatedBy() : "admin")
                .date(sm.getCreatedAt())
                .createdAt(sm.getCreatedAt())
                .updatedAt(sm.getUpdatedAt())
                .sourceWarehouseId(sm.getSourceWarehouse() != null ? sm.getSourceWarehouse().getId() : null)
                .sourceWarehouseName(sm.getSourceWarehouse() != null ? sm.getSourceWarehouse().getName() : null)
                .targetWarehouseId(sm.getTargetWarehouse() != null ? sm.getTargetWarehouse().getId() : null)
                .targetWarehouseName(sm.getTargetWarehouse() != null ? sm.getTargetWarehouse().getName() : null)
                .referenceType(sm.getReferenceType())
                .referenceId(sm.getReferenceId())
                .unitCost(sm.getUnitCost())
                .build();
    }
}
