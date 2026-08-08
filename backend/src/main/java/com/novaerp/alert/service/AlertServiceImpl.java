package com.novaerp.alert.service;

import com.novaerp.alert.dto.AlertDTO;
import com.novaerp.alert.entity.Alert;
import com.novaerp.alert.entity.AlertSeverity;
import com.novaerp.alert.entity.AlertType;
import com.novaerp.alert.repository.AlertRepository;
import com.novaerp.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AlertDTO> getAlerts(boolean unreadOnly) {
        log.info("Fetching alerts (unreadOnly: {})", unreadOnly);
        List<Alert> alerts = unreadOnly
                ? alertRepository.findByIsReadFalseOrderByCreatedAtDesc()
                : alertRepository.findAllByOrderByCreatedAtDesc();
        return alerts.stream().map(AlertDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AlertDTO> getAlertsPaginated(Pageable pageable, Boolean unreadOnly) {
        Page<Alert> page = (unreadOnly != null && unreadOnly)
                ? alertRepository.findByIsRead(false, pageable)
                : alertRepository.findAll(pageable);
        return page.map(AlertDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public AlertDTO getAlertById(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));
        return AlertDTO.fromEntity(alert);
    }

    @Override
    @Transactional
    public AlertDTO createAlert(AlertDTO dto) {
        log.info("Creating alert: {}", dto.getTitre());

        Alert alert = Alert.builder()
                .type(dto.getType() != null ? dto.getType() : AlertType.SYSTEME)
                .title(dto.getTitre())
                .message(dto.getMessage())
                .severity(dto.getNiveau() != null ? dto.getNiveau() : AlertSeverity.INFO)
                .isRead(dto.getLu() != null ? dto.getLu() : false)
                .entityType(dto.getEntiteType())
                .entityId(dto.getEntiteId())
                .build();

        Alert saved = alertRepository.save(alert);
        return AlertDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public void markAsRead(Long id) {
        log.info("Marking alert ID {} as read", id);
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));
        alert.setIsRead(true);
        alertRepository.save(alert);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        log.info("Marking all alerts as read");
        alertRepository.markAllAsRead();
    }

    @Override
    @Transactional
    public void deleteAlert(Long id) {
        log.info("Deleting alert ID: {}", id);
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));
        alertRepository.delete(alert);
    }
}
