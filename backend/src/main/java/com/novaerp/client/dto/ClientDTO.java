package com.novaerp.client.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.novaerp.client.entity.Client;
import com.novaerp.client.entity.ClientStatus;
import jakarta.validation.constraints.NotBlank;
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
public class ClientDTO {

    private Long id;

    private String code;

    @NotBlank(message = "Client name is required")
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

    private String companyName;

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

    private String city;
    private String country;
    private String taxNumber;
    private BigDecimal creditLimit;
    private ClientStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static ClientDTO fromEntity(Client client) {
        return ClientDTO.builder()
                .id(client.getId())
                .code(client.getCode())
                .nom(client.getName())
                .companyName(client.getCompanyName())
                .email(client.getEmail())
                .telephone(client.getPhone())
                .adresse(client.getAddress())
                .city(client.getCity())
                .country(client.getCountry())
                .taxNumber(client.getTaxNumber())
                .creditLimit(client.getCreditLimit())
                .status(client.getStatus())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .build();
    }
}
