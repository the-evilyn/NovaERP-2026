package com.novaerp.ai.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.novaerp.ai.entity.AiAnomaly;
import com.novaerp.ai.entity.AnomalySeverity;
import com.novaerp.ai.entity.AnomalyStatus;
import com.novaerp.ai.entity.AnomalyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAnomalyDTO {

    private Long id;

    @JsonProperty("type")
    private AnomalyType type;

    @JsonProperty("severite")
    @JsonAlias({"severite", "severity"})
    private AnomalySeverity severite;

    @JsonProperty("severity")
    public AnomalySeverity getSeverity() {
        return severite;
    }

    public void setSeverity(AnomalySeverity severity) {
        this.severite = severity;
    }

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

    @JsonProperty("description")
    private String description;

    @JsonProperty("dateDetection")
    @JsonAlias({"dateDetection", "detectionDate"})
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateDetection;

    @JsonProperty("entiteType")
    @JsonAlias({"entiteType", "entityType"})
    private String entiteType;

    @JsonProperty("entiteId")
    @JsonAlias({"entiteId", "entityId"})
    private Long entiteId;

    @JsonProperty("statut")
    @JsonAlias({"statut", "status"})
    @Builder.Default
    private AnomalyStatus statut = AnomalyStatus.NOUVEAU;

    @JsonProperty("status")
    public AnomalyStatus getStatus() {
        return statut;
    }

    public void setStatus(AnomalyStatus status) {
        this.statut = status;
    }

    public static AiAnomalyDTO fromEntity(AiAnomaly a) {
        return AiAnomalyDTO.builder()
                .id(a.getId())
                .type(a.getType())
                .severite(a.getSeverity())
                .titre(a.getTitle())
                .description(a.getDescription())
                .dateDetection(a.getDetectionDate())
                .entiteType(a.getEntityType())
                .entiteId(a.getEntityId())
                .statut(a.getStatus())
                .build();
    }
}
