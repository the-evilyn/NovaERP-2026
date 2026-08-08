package com.novaerp.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novaerp.audit.controller.AuditLogController;
import com.novaerp.audit.dto.AuditLogDTO;
import com.novaerp.audit.entity.AuditAction;
import com.novaerp.audit.service.AuditLogService;
import com.novaerp.security.jwt.CustomAccessDeniedHandler;
import com.novaerp.security.jwt.JwtAuthenticationEntryPoint;
import com.novaerp.security.jwt.JwtAuthenticationFilter;
import com.novaerp.security.jwt.JwtTokenProvider;
import com.novaerp.security.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditLogController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuditLogService auditLogService;

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
    void testGetAuditLogsEndpoint() throws Exception {
        AuditLogDTO dto = AuditLogDTO.builder()
                .id(1L)
                .action(AuditAction.CONNEXION)
                .entite("USER")
                .utilisateurNom("Admin User")
                .date(LocalDateTime.now())
                .build();

        when(auditLogService.getAuditLogs(any(Pageable.class), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/audit-logs?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].action").value("CONNEXION"))
                .andExpect(jsonPath("$.content[0].utilisateurNom").value("Admin User"));
    }

    @Test
    void testExportAuditLogsEndpoint() throws Exception {
        when(auditLogService.exportAuditLogsCsv()).thenReturn("ID,Date,User,Action\n1,2026-08-08,Admin,CONNEXION".getBytes());

        mockMvc.perform(get("/audit-logs/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=audit-logs.csv"))
                .andExpect(content().contentType("text/csv"));
    }
}
