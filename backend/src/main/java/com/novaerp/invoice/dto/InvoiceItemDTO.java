package com.novaerp.invoice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.novaerp.invoice.entity.InvoiceItem;
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
public class InvoiceItemDTO {

    private Long id;

    @NotNull(message = "Product ID is required")
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

    @NotNull(message = "Quantity is required")
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

    public static InvoiceItemDTO fromEntity(InvoiceItem item) {
        return InvoiceItemDTO.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productNom(item.getProduct().getName())
                .quantite(item.getQuantity())
                .prixUnitaire(item.getUnitPrice())
                .total(item.getTotalAmount() != null ? item.getTotalAmount() : item.getSubtotal())
                .build();
    }
}
