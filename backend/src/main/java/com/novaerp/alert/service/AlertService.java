package com.novaerp.alert.service;

import com.novaerp.alert.dto.AlertDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AlertService {
    List<AlertDTO> getAlerts(boolean unreadOnly);
    Page<AlertDTO> getAlertsPaginated(Pageable pageable, Boolean unreadOnly);
    AlertDTO getAlertById(Long id);
    AlertDTO createAlert(AlertDTO dto);
    void markAsRead(Long id);
    void markAllAsRead();
    void deleteAlert(Long id);
}
