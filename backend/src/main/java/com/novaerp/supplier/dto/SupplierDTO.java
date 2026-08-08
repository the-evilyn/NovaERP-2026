package com.novaerp.supplier.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.novaerp.supplier.entity.Supplier;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDTO {

    private Long id;

    @JsonProperty("code")
    private String code;

    @NotBlank(message = "Supplier name is required")
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

    @JsonProperty("email")
    private String email;

    @JsonProperty("telephone")
    @JsonAlias({"phone", "telephone"})
    private String telephone;

    @JsonProperty("phone")
    public String getPhone() {
        return telephone;
    }

    public void setPhone(String phone) {
        this.telephone = phone;
    }

    @JsonProperty("adresse")
    @JsonAlias({"address", "adresse"})
    private String adresse;

    @JsonProperty("address")
    public String getAddress() {
        return adresse;
    }

    public void setAddress(String address) {
        this.adresse = address;
    }

    @JsonProperty("ice")
    private String ice;

    private String taxId;
    private String contactName;
    private String city;
    private String country;
    private Integer paymentTerms;
    private boolean active;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static SupplierDTO fromEntity(Supplier supplier) {
        return SupplierDTO.builder()
                .id(supplier.getId())
                .code(supplier.getCode())
                .nom(supplier.getName())
                .email(supplier.getEmail())
                .telephone(supplier.getPhone())
                .adresse(supplier.getAddress())
                .ice(supplier.getIce())
                .taxId(supplier.getTaxId())
                .contactName(supplier.getContactName())
                .city(supplier.getCity())
                .country(supplier.getCountry())
                .paymentTerms(supplier.getPaymentTerms())
                .active(supplier.isActive())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }
}
