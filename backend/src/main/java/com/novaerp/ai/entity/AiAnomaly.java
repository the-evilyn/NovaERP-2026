package com.novaerp.ai.entity;

import com.novaerp.common.entity.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_anomalies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, of = "id")
public class AiAnomaly extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = false)
    private AnomalyType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 20, nullable = false)
    @Builder.Default
    private AnomalySeverity severity = AnomalySeverity.MOYENNE;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private AnomalyStatus status = AnomalyStatus.NOUVEAU;

    @Column(name = "detection_date", nullable = false)
    @Builder.Default
    private LocalDateTime detectionDate = LocalDateTime.now();
}
