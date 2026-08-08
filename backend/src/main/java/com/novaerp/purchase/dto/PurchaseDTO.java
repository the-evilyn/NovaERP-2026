package com.novaerp.purchase.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.novaerp.purchase.entity.PurchaseOrder;
import com.novaerp.purchase.entity.PurchaseStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseDTO {

    private Long id;

    @JsonProperty("reference")
    @JsonAlias({"reference", "orderNumber"})
    private String reference;

    @JsonProperty("orderNumber")
    public String getOrderNumber() {
        return reference;
    }

    public void setOrderNumber(String orderNumber) {
        this.reference = orderNumber;
    }

    @NotNull(message = "Supplier ID is required")
    @JsonProperty("fournisseurId")
    @JsonAlias({"fournisseurId", "supplierId"})
    private Long fournisseurId;

    @JsonProperty("supplierId")
    public Long getSupplierId() {
        return fournisseurId;
    }

    public void setSupplierId(Long supplierId) {
        this.fournisseurId = supplierId;
    }

    @JsonProperty("fournisseurNom")
    @JsonAlias({"fournisseurNom", "supplierName"})
    private String fournisseurNom;

    @JsonProperty("supplierName")
    public String getSupplierName() {
        return fournisseurNom;
    }

    public void setSupplierName(String supplierName) {
        this.fournisseurNom = supplierName;
    }

    @JsonProperty("date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @JsonProperty("statut")
    @JsonAlias({"statut", "status"})
    @Builder.Default
    private PurchaseStatus statut = PurchaseStatus.EN_ATTENTE;

    @JsonProperty("status")
    public PurchaseStatus getStatus() {
        return statut;
    }

    public void setStatus(PurchaseStatus status) {
        this.statut = status;
    }

    @JsonProperty("totalHT")
    @JsonAlias({"totalHT", "subtotal"})
    private BigDecimal totalHT;

    @JsonProperty("subtotal")
    public BigDecimal getSubtotal() {
        return totalHT;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.totalHT = subtotal;
    }

    @JsonProperty("tva")
    @JsonAlias({"tva", "taxRate", "taxAmount"})
    private BigDecimal tva;

    @JsonProperty("totalTTC")
    @JsonAlias({"totalTTC", "totalAmount"})
    private BigDecimal totalTTC;

    @JsonProperty("totalAmount")
    public BigDecimal getTotalAmount() {
        return totalTTC;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalTTC = totalAmount;
    }

    private Long warehouseId;
    private String notes;

    @JsonProperty("items")
    @Builder.Default
    private List<PurchaseItemDTO> items = new ArrayList<>();

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static PurchaseDTO fromEntity(PurchaseOrder order) {
        return PurchaseDTO.builder()
                .id(order.getId())
                .reference(order.getOrderNumber())
                .fournisseurId(order.getSupplier().getId())
                .fournisseurNom(order.getSupplier().getName())
                .date(order.getOrderDate())
                .statut(order.getStatus())
                .totalHT(order.getSubtotal())
                .tva(order.getTaxRate())
                .totalTTC(order.getTotalAmount())
                .warehouseId(order.getWarehouse() != null ? order.getWarehouse().getId() : null)
                .notes(order.getNotes())
                .items(order.getItems().stream().map(PurchaseItemDTO::fromEntity).collect(Collectors.toList()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
