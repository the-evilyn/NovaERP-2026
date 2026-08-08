package com.novaerp.sale.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.novaerp.sale.entity.SaleStatus;
import com.novaerp.sale.entity.SalesOrder;
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
public class SaleDTO {

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

    @NotNull(message = "Client ID is required")
    @JsonProperty("clientId")
    private Long clientId;

    @JsonProperty("clientNom")
    @JsonAlias({"clientNom", "clientName"})
    private String clientNom;

    @JsonProperty("clientName")
    public String getClientName() {
        return clientNom;
    }

    public void setClientName(String clientName) {
        this.clientNom = clientName;
    }

    @JsonProperty("date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @JsonProperty("statut")
    @JsonAlias({"statut", "status"})
    @Builder.Default
    private SaleStatus statut = SaleStatus.COMMANDE;

    @JsonProperty("status")
    public SaleStatus getStatus() {
        return statut;
    }

    public void setStatus(SaleStatus status) {
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
    private BigDecimal discountAmount;
    private BigDecimal shippingCost;
    private String notes;

    @JsonProperty("items")
    @Builder.Default
    private List<SaleItemDTO> items = new ArrayList<>();

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static SaleDTO fromEntity(SalesOrder order) {
        return SaleDTO.builder()
                .id(order.getId())
                .reference(order.getOrderNumber())
                .clientId(order.getClient().getId())
                .clientNom(order.getClient().getName())
                .date(order.getOrderDate())
                .statut(order.getStatus())
                .totalHT(order.getSubtotal())
                .tva(order.getTaxRate())
                .totalTTC(order.getTotalAmount())
                .warehouseId(order.getWarehouse() != null ? order.getWarehouse().getId() : null)
                .discountAmount(order.getDiscountAmount())
                .shippingCost(order.getShippingCost())
                .notes(order.getNotes())
                .items(order.getItems().stream().map(SaleItemDTO::fromEntity).collect(Collectors.toList()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
