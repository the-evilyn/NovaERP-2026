package com.novaerp.audit.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.novaerp.audit.entity.AuditAction;
import com.novaerp.audit.entity.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {

    private Long id;

    @JsonProperty("userId")
    @JsonAlias({"userId", "utilisateurId"})
    private Long userId;

    @JsonProperty("utilisateurId")
    public Long getUtilisateurId() {
        return userId;
    }

    public void setUtilisateurId(Long utilisateurId) {
        this.userId = utilisateurId;
    }

    @JsonProperty("username")
    @JsonAlias({"username", "utilisateurNom", "userName"})
    private String username;

    @JsonProperty("utilisateurNom")
    public String getUtilisateurNom() {
        return username;
    }

    public void setUtilisateurNom(String utilisateurNom) {
        this.username = utilisateurNom;
    }

    @JsonProperty("userName")
    public String getUserName() {
        return username;
    }

    public void setUserName(String userName) {
        this.username = userName;
    }

    @JsonProperty("action")
    private AuditAction action;

    @JsonProperty("entityType")
    @JsonAlias({"entityType", "entite", "entity"})
    private String entityType;

    @JsonProperty("entite")
    public String getEntite() {
        return entityType;
    }

    public void setEntite(String entite) {
        this.entityType = entite;
    }

    @JsonProperty("entityId")
    @JsonAlias({"entityId", "entiteId"})
    private Long entityId;

    @JsonProperty("entiteId")
    public Long getEntiteId() {
        return entityId;
    }

    public void setEntiteId(Long entiteId) {
        this.entityId = entiteId;
    }

    @JsonProperty("ancienneValeur")
    private String ancienneValeur;

    @JsonProperty("nouvelleValeur")
    private String nouvelleValeur;

    @JsonProperty("details")
    private String details;

    @JsonProperty("ipAddress")
    @JsonAlias({"ipAddress", "ip_address"})
    private String ipAddress;

    @JsonProperty("date")
    @JsonAlias({"date", "timestamp"})
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static AuditLogDTO fromEntity(AuditLog a) {
        Long uId = a.getUser() != null ? a.getUser().getId() : 1L;
        String uName = a.getUserName() != null ? a.getUserName() : (a.getUser() != null ? a.getUser().getUsername() : "salma");

        return AuditLogDTO.builder()
                .id(a.getId())
                .userId(uId)
                .username(uName)
                .action(a.getAction())
                .entityType(a.getEntityType())
                .entityId(a.getEntityId())
                .ancienneValeur(null)
                .nouvelleValeur(a.getDetails())
                .details(a.getDetails())
                .ipAddress(a.getIpAddress())
                .date(a.getTimestamp())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getCreatedAt())
                .build();
    }
}
