package com.novaerp.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.novaerp.dashboard.controller.DashboardController;
import com.novaerp.dashboard.dto.DashboardStatsDTO;
import com.novaerp.dashboard.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void testGetDashboardStatsEndpoint() throws Exception {
        DashboardStatsDTO stats = DashboardStatsDTO.builder()
                .totalVentes(BigDecimal.valueOf(150000.0))
                .totalAchats(BigDecimal.valueOf(95000.0))
                .totalClients(12L)
                .totalFournisseurs(8L)
                .build();

        when(dashboardService.getDashboardStats()).thenReturn(stats);

        mockMvc.perform(get("/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVentes").value(150000.0))
                .andExpect(jsonPath("$.totalClients").value(12));
    }

    @Test
    void testGetSalesTrendsEndpoint() throws Exception {
        when(dashboardService.getSalesTrends()).thenReturn(List.of());

        mockMvc.perform(get("/dashboard/sales-trends"))
                .andExpect(status().isOk());
    }
}
