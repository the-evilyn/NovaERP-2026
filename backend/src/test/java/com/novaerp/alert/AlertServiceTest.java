package com.novaerp.alert;

import com.novaerp.alert.dto.AlertDTO;
import com.novaerp.alert.entity.Alert;
import com.novaerp.alert.entity.AlertSeverity;
import com.novaerp.alert.entity.AlertType;
import com.novaerp.alert.repository.AlertRepository;
import com.novaerp.alert.service.AlertServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private AlertServiceImpl alertService;

    private Alert sampleAlert;

    @BeforeEach
    void setUp() {
        sampleAlert = Alert.builder()
                .id(1L)
                .type(AlertType.STOCK_BAS)
                .title("Stock critique")
                .message("Riz Parfumé sous le seuil")
                .severity(AlertSeverity.DANGER)
                .isRead(false)
                .build();
    }

    @Test
    void testGetAlertsUnreadOnly() {
        when(alertRepository.findByIsReadFalseOrderByCreatedAtDesc()).thenReturn(List.of(sampleAlert));

        List<AlertDTO> result = alertService.getAlerts(true);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Stock critique", result.get(0).getTitre());
        assertFalse(result.get(0).getLu());
    }

    @Test
    void testCreateAlert() {
        AlertDTO input = AlertDTO.builder()
                .titre("Nouveau client")
                .message("Client Marjane créé")
                .niveau(AlertSeverity.INFO)
                .build();

        when(alertRepository.save(any(Alert.class))).thenAnswer(i -> {
            Alert a = i.getArgument(0);
            a.setId(2L);
            return a;
        });

        AlertDTO result = alertService.createAlert(input);

        assertNotNull(result);
        assertEquals("Nouveau client", result.getTitre());
        assertEquals(AlertSeverity.INFO, result.getNiveau());
    }

    @Test
    void testMarkAsRead() {
        when(alertRepository.findById(1L)).thenReturn(Optional.of(sampleAlert));
        when(alertRepository.save(any(Alert.class))).thenReturn(sampleAlert);

        alertService.markAsRead(1L);

        assertTrue(sampleAlert.getIsRead());
        verify(alertRepository, times(1)).save(sampleAlert);
    }
}
