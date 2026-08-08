package com.novaerp.alert.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.novaerp.alert.entity.Alert;
import com.novaerp.alert.entity.AlertSeverity;
import com.novaerp.alert.entity.AlertType;
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
public class AlertDTO {

    private Long id;

    @JsonProperty("type")
    @Builder.Default
    private AlertType type = AlertType.SYSTEME;

    @NotBlank(message = "Title is required")
    @JsonProperty("titre")
    @JsonAlias({"titre", "title"})
    private String titre;

    @JsonProperty("title")
    public String getTitle() {
        return titre;
    }

    public void setTitle(String title) {
        this.titre = title;
    }

    @NotBlank(message = "Message is required")
    @JsonProperty("message")
    private String message;

    @JsonProperty("niveau")
    @JsonAlias({"niveau", "severity"})
    @Builder.Default
    private AlertSeverity niveau = AlertSeverity.INFO;

    @JsonProperty("severity")
    public AlertSeverity getSeverity() {
        return niveau;
    }

    public void setSeverity(AlertSeverity severity) {
        this.niveau = severity;
    }

    @JsonProperty("lu")
    @JsonAlias({"lu", "isRead", "read"})
    @Builder.Default
    private Boolean lu = false;

    @JsonProperty("isRead")
    public Boolean getIsRead() {
        return lu;
    }

    public void setIsRead(Boolean isRead) {
        this.lu = isRead;
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

    @JsonProperty("entiteType")
    @JsonAlias({"entiteType", "entityType"})
    private String entiteType;

    @JsonProperty("entityType")
    public String getEntityType() {
        return entiteType;
    }

    public void setEntityType(String entityType) {
        this.entiteType = entityType;
    }

    @JsonProperty("dateCreation")
    @JsonAlias({"dateCreation", "createdAt"})
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateCreation;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static AlertDTO fromEntity(Alert a) {
        return AlertDTO.builder()
                .id(a.getId())
                .type(a.getType())
                .titre(a.getTitle())
                .message(a.getMessage())
                .niveau(a.getSeverity())
                .lu(a.getIsRead())
                .entiteId(a.getEntityId())
                .entiteType(a.getEntityType())
                .dateCreation(a.getCreatedAt())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
