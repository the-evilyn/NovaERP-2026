package com.novaerp.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novaerp.dashboard.controller.DashboardController;
import com.novaerp.dashboard.dto.DashboardStatsDTO;
import com.novaerp.dashboard.service.DashboardService;
import com.novaerp.security.jwt.CustomAccessDeniedHandler;
import com.novaerp.security.jwt.JwtAuthenticationEntryPoint;
import com.novaerp.security.jwt.JwtAuthenticationFilter;
import com.novaerp.security.jwt.JwtTokenProvider;
import com.novaerp.security.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DashboardService dashboardService;

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
