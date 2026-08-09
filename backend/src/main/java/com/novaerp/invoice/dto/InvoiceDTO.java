package com.novaerp.invoice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.novaerp.invoice.entity.Invoice;
import com.novaerp.invoice.entity.InvoiceStatus;
import com.novaerp.invoice.entity.InvoiceType;
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
public class InvoiceDTO {

    private Long id;

    @JsonProperty("numero")
    @JsonAlias({"numero", "reference", "invoiceNumber"})
    private String numero;

    @JsonProperty("reference")
    public String getReference() {
        return numero;
    }

    public void setReference(String reference) {
        this.numero = reference;
    }

    @JsonProperty("invoiceNumber")
    public String getInvoiceNumber() {
        return numero;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.numero = invoiceNumber;
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

    private Long salesOrderId;

    @JsonProperty("type")
    @Builder.Default
    private InvoiceType type = InvoiceType.STANDARD;

    @JsonProperty("statut")
    @JsonAlias({"statut", "status"})
    @Builder.Default
    private InvoiceStatus statut = InvoiceStatus.VALIDEE;

    @JsonProperty("status")
    public InvoiceStatus getStatus() {
        return statut;
    }

    public void setStatus(InvoiceStatus status) {
        this.statut = status;
    }

    @JsonProperty("date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @JsonProperty("dateEcheance")
    @JsonAlias({"dateEcheance", "dueDate"})
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateEcheance;

    @JsonProperty("dueDate")
    public LocalDate getDueDate() {
        return dateEcheance;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dateEcheance = dueDate;
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

    @JsonProperty("montantPaye")
    @JsonAlias({"montantPaye", "paidAmount"})
    @Builder.Default
    private BigDecimal montantPaye = BigDecimal.ZERO;

    @JsonProperty("paidAmount")
    public BigDecimal getPaidAmount() {
        return montantPaye;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.montantPaye = paidAmount;
    }

    private BigDecimal discountAmount;
    private String notes;

    @JsonProperty("lignes")
    @JsonAlias({"lignes", "items"})
    @Builder.Default
    private List<InvoiceItemDTO> lignes = new ArrayList<>();

    @JsonProperty("items")
    public List<InvoiceItemDTO> getItems() {
        return lignes;
    }

    public void setItems(List<InvoiceItemDTO> items) {
        this.lignes = items;
    }

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static InvoiceDTO fromEntity(Invoice invoice) {
        return InvoiceDTO.builder()
                .id(invoice.getId())
                .numero(invoice.getInvoiceNumber())
                .clientId(invoice.getClient().getId())
                .clientNom(invoice.getClient().getName())
                .salesOrderId(invoice.getSalesOrder() != null ? invoice.getSalesOrder().getId() : null)
                .type(invoice.getType())
                .statut(invoice.getStatus())
                .date(invoice.getIssueDate())
                .dateEcheance(invoice.getDueDate())
                .totalHT(invoice.getSubtotal())
                .tva(invoice.getTaxRate())
                .totalTTC(invoice.getTotalAmount())
                .montantPaye(invoice.getPaidAmount())
                .discountAmount(invoice.getDiscountAmount())
                .notes(invoice.getNotes())
                .lignes(invoice.getItems().stream().map(InvoiceItemDTO::fromEntity).collect(Collectors.toList()))
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }
}
