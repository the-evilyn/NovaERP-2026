package com.novaerp.sale.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.novaerp.sale.entity.SalesOrderItem;
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
public class SaleItemDTO {

    private Long id;

    @NotNull(message = "Product ID is required")
    @JsonProperty("produitId")
    @JsonAlias({"produitId", "productId"})
    private Long produitId;

    @JsonProperty("productId")
    public Long getProductId() {
        return produitId;
    }

    public void setProductId(Long productId) {
        this.produitId = productId;
    }

    @JsonProperty("produitNom")
    @JsonAlias({"produitNom", "productName"})
    private String produitNom;

    @JsonProperty("productName")
    public String getProductName() {
        return produitNom;
    }

    public void setProductName(String productName) {
        this.produitNom = productName;
    }

    @NotNull(message = "Quantity is required")
    @JsonProperty("quantite")
    @JsonAlias({"quantite", "quantity", "quantityOrdered"})
    private BigDecimal quantite;

    @JsonProperty("quantity")
    public BigDecimal getQuantity() {
        return quantite;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantite = quantity;
    }

    @NotNull(message = "Unit price is required")
    @JsonProperty("prixUnitaire")
    @JsonAlias({"prixUnitaire", "unitPrice"})
    private BigDecimal prixUnitaire;

    @JsonProperty("unitPrice")
    public BigDecimal getUnitPrice() {
        return prixUnitaire;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.prixUnitaire = unitPrice;
    }

    @JsonProperty("total")
    @JsonAlias({"total", "subtotal", "totalAmount"})
    private BigDecimal total;

    public static SaleItemDTO fromEntity(SalesOrderItem item) {
        return SaleItemDTO.builder()
                .id(item.getId())
                .produitId(item.getProduct().getId())
                .produitNom(item.getProduct().getName())
                .quantite(item.getQuantityOrdered())
                .prixUnitaire(item.getUnitPrice())
                .total(item.getTotalAmount() != null ? item.getTotalAmount() : item.getSubtotal())
                .build();
    }
}
