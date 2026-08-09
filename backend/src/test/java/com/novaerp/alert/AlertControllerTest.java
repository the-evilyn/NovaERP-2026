package com.novaerp.alert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.novaerp.alert.controller.AlertController;
import com.novaerp.alert.dto.AlertDTO;
import com.novaerp.alert.entity.AlertSeverity;
import com.novaerp.alert.entity.AlertType;
import com.novaerp.alert.service.AlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AlertControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AlertService alertService;

    @InjectMocks
    private AlertController alertController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(alertController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void testGetAlertsEndpoint() throws Exception {
        AlertDTO alert = AlertDTO.builder()
                .id(1L)
                .type(AlertType.STOCK_BAS)
                .titre("Stock critique")
                .message("Riz Parfumé sous le seuil")
                .niveau(AlertSeverity.DANGER)
                .lu(false)
                .build();

        when(alertService.getAlerts(false)).thenReturn(List.of(alert));

        mockMvc.perform(get("/alerts?unreadOnly=false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titre").value("Stock critique"))
                .andExpect(jsonPath("$[0].niveau").value("DANGER"));
    }

    @Test
    void testCreateAlertEndpoint() throws Exception {
        AlertDTO input = AlertDTO.builder()
                .titre("Rupture de stock")
                .message("Farine T55 épuisée")
                .niveau(AlertSeverity.DANGER)
                .build();

        AlertDTO output = AlertDTO.builder()
                .id(1L)
                .titre("Rupture de stock")
                .message("Farine T55 épuisée")
                .niveau(AlertSeverity.DANGER)
                .build();

        when(alertService.createAlert(any(AlertDTO.class))).thenReturn(output);

        mockMvc.perform(post("/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titre").value("Rupture de stock"));
    }
}
