package com.novaerp.product.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.novaerp.product.entity.Product;
import com.novaerp.product.entity.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ProductDTO {

    private Long id;

    @NotBlank(message = "Product name is required")
    @JsonProperty("nom")
    @JsonAlias({"name", "nom"})
    private String nom;

    @JsonProperty("name")
    public String getName() {
        return nom;
    }

    public void setName(String name) {
        this.nom = name;
    }

    @NotBlank(message = "Product reference / SKU is required")
    @JsonProperty("reference")
    @JsonAlias({"sku", "reference"})
    private String reference;

    @JsonProperty("sku")
    public String getSku() {
        return reference;
    }

    public void setSku(String sku) {
        this.reference = sku;
    }

    @NotNull(message = "Purchase price is required")
    @JsonProperty("prixAchat")
    @JsonAlias({"purchasePrice", "prixAchat"})
    private BigDecimal prixAchat;

    @JsonProperty("purchasePrice")
    public BigDecimal getPurchasePrice() {
        return prixAchat;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.prixAchat = purchasePrice;
    }

    @NotNull(message = "Selling price is required")
    @JsonProperty("prixVente")
    @JsonAlias({"sellingPrice", "prixVente"})
    private BigDecimal prixVente;

    @JsonProperty("sellingPrice")
    public BigDecimal getSellingPrice() {
        return prixVente;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.prixVente = sellingPrice;
    }

    @JsonProperty("quantiteStock")
    @JsonAlias({"quantiteStock", "stockQuantity", "quantity"})
    @Builder.Default
    private BigDecimal quantiteStock = BigDecimal.ZERO;

    @JsonProperty("stockQuantity")
    public BigDecimal getStockQuantity() {
        return quantiteStock;
    }

    public void setStockQuantity(BigDecimal stockQuantity) {
        this.quantiteStock = stockQuantity;
    }

    @JsonProperty("seuilMinimum")
    @JsonAlias({"minStockLevel", "seuilMinimum"})
    @Builder.Default
    private BigDecimal seuilMinimum = BigDecimal.ZERO;

    @JsonProperty("minStockLevel")
    public BigDecimal getMinStockLevel() {
        return seuilMinimum;
    }

    public void setMinStockLevel(BigDecimal minStockLevel) {
        this.seuilMinimum = minStockLevel;
    }

    @JsonProperty("categorie")
    @JsonAlias({"category", "categorie", "categoryName"})
    private String categorie;

    @JsonProperty("categoryName")
    public String getCategoryName() {
        return categorie;
    }

    public void setCategoryName(String categoryName) {
        this.categorie = categoryName;
    }

    private Long categoryId;
    private String barcode;
    private String description;
    private String unitOfMeasure;
    private ProductStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static ProductDTO fromEntity(Product product, BigDecimal currentStock) {
        return ProductDTO.builder()
                .id(product.getId())
                .nom(product.getName())
                .reference(product.getSku())
                .prixAchat(product.getPurchasePrice())
                .prixVente(product.getSellingPrice())
                .quantiteStock(currentStock != null ? currentStock : BigDecimal.ZERO)
                .seuilMinimum(product.getMinStockLevel())
                .categorie(product.getCategory() != null ? product.getCategory().getName() : null)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .barcode(product.getBarcode())
                .description(product.getDescription())
                .unitOfMeasure(product.getUnitOfMeasure())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
