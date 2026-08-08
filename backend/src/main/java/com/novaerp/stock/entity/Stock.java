package com.novaerp.stock.entity;

import com.novaerp.common.entity.BaseAuditableEntity;
import com.novaerp.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "stock", uniqueConstraints = {
        @UniqueConstraint(name = "uk_stock_product_warehouse", columnNames = {"product_id", "warehouse_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, of = "id")
public class Stock extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "quantity_on_hand", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal quantityOnHand = BigDecimal.ZERO;

    @Column(name = "quantity_allocated", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal quantityAllocated = BigDecimal.ZERO;

    @Column(name = "quantity_available", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal quantityAvailable = BigDecimal.ZERO;

    public void recalculateAvailable() {
        this.quantityAvailable = this.quantityOnHand.subtract(this.quantityAllocated);
    }
}
