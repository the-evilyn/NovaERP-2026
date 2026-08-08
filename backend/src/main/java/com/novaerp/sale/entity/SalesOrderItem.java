package com.novaerp.sale.entity;

import com.novaerp.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sales_order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class SalesOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrder salesOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity_ordered", precision = 12, scale = 2, nullable = false)
    private BigDecimal quantityOrdered;

    @Column(name = "quantity_shipped", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal quantityShipped = BigDecimal.ZERO;

    @Column(name = "unit_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "discount_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountRate = BigDecimal.ZERO;

    @Column(name = "subtotal", precision = 15, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @Column(name = "tax_rate", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.valueOf(20.00);

    @Column(name = "tax_amount", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    public void recalculateTotals() {
        BigDecimal base = this.quantityOrdered.multiply(this.unitPrice);
        if (this.discountRate != null && this.discountRate.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal disc = base.multiply(this.discountRate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            base = base.subtract(disc);
        }
        this.subtotal = base;
        this.taxAmount = this.subtotal.multiply(this.taxRate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        this.totalAmount = this.subtotal.add(this.taxAmount);
    }
}
