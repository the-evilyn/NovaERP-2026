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

    @JsonProperty("produitId")
    @JsonAlias({"produitId", "productId"})
    private Long productId;

    @JsonProperty("produitNom")
    @JsonAlias({"produitNom", "productName"})
    private String productName;

    @JsonProperty("type")
    private String type; // ENTREE, SORTIE, AJUSTEMENT, TRANSFERT

    @JsonProperty("quantite")
    @JsonAlias({"quantite", "quantity"})
    private BigDecimal quantite;

    @JsonProperty("motif")
    @JsonAlias({"motif", "reason", "notes"})
    private String motif;

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

    public static StockMovementDTO fromEntity(StockMovement sm) {
        String frontendType = switch (sm.getMovementType()) {
            case IN_PURCHASE -> "ENTREE";
            case OUT_SALE -> "SORTIE";
            case ADJUSTMENT_IN, ADJUSTMENT_OUT -> "AJUSTEMENT";
            case TRANSFER -> "TRANSFERT";
            case RETURN -> "RETOUR";
        };

        return StockMovementDTO.builder()
                .id(sm.getId())
                .productId(sm.getProduct().getId())
                .productName(sm.getProduct().getName())
                .type(frontendType)
                .quantite(sm.getQuantity())
                .motif(sm.getNotes() != null ? sm.getNotes() : sm.getReferenceId())
                .date(sm.getCreatedAt())
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
