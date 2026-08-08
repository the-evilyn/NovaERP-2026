package com.novaerp.payment.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.novaerp.payment.entity.Payment;
import com.novaerp.payment.entity.PaymentMethod;
import com.novaerp.payment.entity.PaymentStatus;
import com.novaerp.payment.entity.PaymentType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {

    private Long id;

    @JsonProperty("reference")
    @JsonAlias({"reference", "paymentNumber"})
    private String reference;

    @JsonProperty("paymentNumber")
    public String getPaymentNumber() {
        return reference;
    }

    public void setPaymentNumber(String paymentNumber) {
        this.reference = paymentNumber;
    }

    @JsonProperty("factureId")
    @JsonAlias({"factureId", "invoiceId"})
    private Long factureId;

    @JsonProperty("invoiceId")
    public Long getInvoiceId() {
        return factureId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.factureId = invoiceId;
    }

    @JsonProperty("factureReference")
    @JsonAlias({"factureReference", "invoiceNumber"})
    private String factureReference;

    @JsonProperty("invoiceNumber")
    public String getInvoiceNumber() {
        return factureReference;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.factureReference = invoiceNumber;
    }

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

    private Long supplierId;
    private String supplierName;

    @NotNull(message = "Payment amount is required")
    @JsonProperty("montant")
    @JsonAlias({"montant", "amount"})
    private BigDecimal montant;

    @JsonProperty("amount")
    public BigDecimal getAmount() {
        return montant;
    }

    public void setAmount(BigDecimal amount) {
        this.montant = amount;
    }

    @JsonProperty("date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @JsonProperty("modePaiement")
    @JsonAlias({"modePaiement", "method", "paymentMethod"})
    @Builder.Default
    private PaymentMethod modePaiement = PaymentMethod.VIREMENT;

    @JsonProperty("method")
    public PaymentMethod getMethod() {
        return modePaiement;
    }

    public void setMethod(PaymentMethod method) {
        this.modePaiement = method;
    }

    @JsonProperty("statut")
    @JsonAlias({"statut", "status", "paymentStatus"})
    @Builder.Default
    private PaymentStatus statut = PaymentStatus.VALIDE;

    @JsonProperty("status")
    public PaymentStatus getStatus() {
        return statut;
    }

    public void setStatus(PaymentStatus status) {
        this.statut = status;
    }

    @JsonProperty("type")
    @Builder.Default
    private PaymentType type = PaymentType.INBOUND_CUSTOMER;

    @JsonProperty("referenceBancaire")
    @JsonAlias({"referenceBancaire", "referenceNumber"})
    private String referenceBancaire;

    @JsonProperty("notes")
    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static PaymentDTO fromEntity(Payment p) {
        return PaymentDTO.builder()
                .id(p.getId())
                .reference(p.getPaymentNumber())
                .factureId(p.getInvoice() != null ? p.getInvoice().getId() : null)
                .factureReference(p.getInvoice() != null ? p.getInvoice().getInvoiceNumber() : null)
                .clientId(p.getClient() != null ? p.getClient().getId() : null)
                .clientNom(p.getClient() != null ? p.getClient().getName() : null)
                .supplierId(p.getSupplier() != null ? p.getSupplier().getId() : null)
                .supplierName(p.getSupplier() != null ? p.getSupplier().getName() : null)
                .montant(p.getAmount())
                .date(p.getPaymentDate())
                .modePaiement(p.getMethod())
                .statut(p.getStatus())
                .type(p.getType())
                .referenceBancaire(p.getReferenceNumber())
                .notes(p.getNotes())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
