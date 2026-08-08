package com.novaerp.invoice.entity;

import com.novaerp.client.entity.Client;
import com.novaerp.common.entity.BaseAuditableEntity;
import com.novaerp.sale.entity.SalesOrder;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, of = "id")
public class Invoice extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", length = 50, nullable = false, unique = true)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id")
    private SalesOrder salesOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30, nullable = false)
    @Builder.Default
    private InvoiceType type = InvoiceType.STANDARD;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.VALIDEE;

    @Column(name = "issue_date", nullable = false)
    @Builder.Default
    private LocalDate issueDate = LocalDate.now();

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "subtotal", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_rate", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.valueOf(20.00);

    @Column(name = "tax_amount", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "paid_amount", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InvoiceItem> items = new ArrayList<>();

    public void addItem(InvoiceItem item) {
        items.add(item);
        item.setInvoice(this);
    }

    public void removeItem(InvoiceItem item) {
        items.remove(item);
        item.setInvoice(null);
    }

    public void recalculateTotals() {
        this.subtotal = items.stream()
                .map(InvoiceItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.taxAmount = this.subtotal.multiply(this.taxRate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        this.totalAmount = this.subtotal.add(this.taxAmount)
                .subtract(this.discountAmount != null ? this.discountAmount : BigDecimal.ZERO);
    }

    public void updatePaymentStatus(BigDecimal newPaidAmount) {
        this.paidAmount = newPaidAmount != null ? newPaidAmount : BigDecimal.ZERO;
        if (this.paidAmount.compareTo(BigDecimal.ZERO) == 0) {
            this.status = (this.dueDate != null && this.dueDate.isBefore(LocalDate.now())) ? InvoiceStatus.EN_RETARD : InvoiceStatus.VALIDEE;
        } else if (this.paidAmount.compareTo(this.totalAmount) >= 0) {
            this.status = InvoiceStatus.PAYEE;
        } else {
            this.status = InvoiceStatus.PARTIELLEMENT_PAYEE;
        }
    }
}
