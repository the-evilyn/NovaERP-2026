package com.novaerp.alert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novaerp.alert.controller.AlertController;
import com.novaerp.alert.dto.AlertDTO;
import com.novaerp.alert.entity.AlertSeverity;
import com.novaerp.alert.entity.AlertType;
import com.novaerp.alert.service.AlertService;
import com.novaerp.security.jwt.CustomAccessDeniedHandler;
import com.novaerp.security.jwt.JwtAuthenticationEntryPoint;
import com.novaerp.security.jwt.JwtAuthenticationFilter;
import com.novaerp.security.jwt.JwtTokenProvider;
import com.novaerp.security.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlertController.class)
@AutoConfigureMockMvc(addFilters = false)
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AlertService alertService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

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
