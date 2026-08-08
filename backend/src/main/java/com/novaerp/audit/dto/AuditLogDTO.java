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

    @JsonProperty("utilisateurId")
    @JsonAlias({"utilisateurId", "userId"})
    private Long utilisateurId;

    @JsonProperty("userId")
    public Long getUserId() {
        return utilisateurId;
    }

    public void setUserId(Long userId) {
        this.utilisateurId = userId;
    }

    @JsonProperty("utilisateurNom")
    @JsonAlias({"utilisateurNom", "userName"})
    private String utilisateurNom;

    @JsonProperty("userName")
    public String getUserName() {
        return utilisateurNom;
    }

    public void setUserName(String userName) {
        this.utilisateurNom = userName;
    }

    @JsonProperty("action")
    private AuditAction action;

    @JsonProperty("entite")
    @JsonAlias({"entite", "entity", "entityType"})
    private String entite;

    @JsonProperty("entityType")
    public String getEntityType() {
        return entite;
    }

    public void setEntityType(String entityType) {
        this.entite = entityType;
    }

    @JsonProperty("entiteId")
    @JsonAlias({"entiteId", "entityId"})
    private Long entiteId;

    @JsonProperty("entityId")
    public Long getEntityId() {
        return entiteId;
    }

    public void setEntityId(Long entityId) {
        this.entiteId = entityId;
    }

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

    public static AuditLogDTO fromEntity(AuditLog a) {
        return AuditLogDTO.builder()
                .id(a.getId())
                .utilisateurId(a.getUser() != null ? a.getUser().getId() : null)
                .utilisateurNom(a.getUserName() != null ? a.getUserName() : (a.getUser() != null ? a.getUser().getFirstName() + " " + a.getUser().getLastName() : "SYSTEM"))
                .action(a.getAction())
                .entite(a.getEntityType())
                .entiteId(a.getEntityId())
                .details(a.getDetails())
                .ipAddress(a.getIpAddress())
                .date(a.getTimestamp())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
