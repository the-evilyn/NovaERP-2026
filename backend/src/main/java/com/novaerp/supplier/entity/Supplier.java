package com.novaerp.supplier.entity;

import com.novaerp.common.entity.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, of = "id")
public class Supplier extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "tax_id", length = 50)
    private String taxId;

    @Column(name = "ice", length = 50)
    private String ice;

    @Column(name = "contact_name", length = 100)
    private String contactName;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "country", length = 100)
    @Builder.Default
    private String country = "Morocco";

    @Column(name = "payment_terms")
    @Builder.Default
    private Integer paymentTerms = 30;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
