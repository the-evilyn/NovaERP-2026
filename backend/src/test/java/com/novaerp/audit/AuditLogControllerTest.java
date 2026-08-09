package com.novaerp.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.novaerp.audit.controller.AuditLogController;
import com.novaerp.audit.dto.AuditLogDTO;
import com.novaerp.audit.entity.AuditAction;
import com.novaerp.audit.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;
import org.springframework.data.web.config.SpringDataJacksonConfiguration;
import org.springframework.data.web.config.SpringDataWebSettings;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuditLogControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AuditLogController auditLogController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new SpringDataJacksonConfiguration.PageModule(
                        new SpringDataWebSettings(PageSerializationMode.DIRECT)));

        mockMvc = MockMvcBuilders.standaloneSetup(auditLogController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(objectMapper),
                        new ByteArrayHttpMessageConverter(),
                        new StringHttpMessageConverter())
                .build();
    }

    @Test
    void testGetAuditLogsEndpoint() throws Exception {
        AuditLogDTO dto = AuditLogDTO.builder()
                .id(1L)
                .action(AuditAction.CONNEXION)
                .entityType("USER")
                .username("Admin User")
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
        when(auditLogService.exportAuditLogsCsv())
                .thenReturn("ID,Date,User,Action\n1,2026-08-08,Admin,CONNEXION".getBytes());

        mockMvc.perform(get("/audit-logs/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=audit-logs.csv"))
                .andExpect(content().contentType("text/csv"));
    }
}
