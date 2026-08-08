package com.novaerp.audit;

import com.novaerp.audit.dto.AuditLogDTO;
import com.novaerp.audit.entity.AuditAction;
import com.novaerp.audit.entity.AuditLog;
import com.novaerp.audit.repository.AuditLogRepository;
import com.novaerp.audit.service.AuditLogServiceImpl;
import com.novaerp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    private AuditLog sampleLog;

    @BeforeEach
    void setUp() {
        sampleLog = AuditLog.builder()
                .id(1L)
                .userName("Admin User")
                .action(AuditAction.CREATION)
                .entityType("CLIENT")
                .entityId(1L)
                .details("Client créé")
                .ipAddress("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void testGetAuditLogs() {
        Page<AuditLog> page = new PageImpl<>(List.of(sampleLog));
        when(auditLogRepository.searchAuditLogs(any(), any(), any(), any())).thenReturn(page);

        Page<AuditLogDTO> result = auditLogService.getAuditLogs(PageRequest.of(0, 20), null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("CLIENT", result.getContent().get(0).getEntite());
    }

    @Test
    void testGetAuditLogById() {
        when(auditLogRepository.findById(1L)).thenReturn(Optional.of(sampleLog));

        AuditLogDTO result = auditLogService.getAuditLogById(1L);

        assertNotNull(result);
        assertEquals(AuditAction.CREATION, result.getAction());
        assertEquals("Admin User", result.getUtilisateurNom());
    }

    @Test
    void testExportAuditLogsCsv() {
        when(auditLogRepository.findAllByOrderByTimestampDesc()).thenReturn(List.of(sampleLog));

        byte[] csv = auditLogService.exportAuditLogsCsv();

        assertNotNull(csv);
        assertTrue(csv.length > 0);
        String csvContent = new String(csv);
        assertTrue(csvContent.contains("Admin User"));
        assertTrue(csvContent.contains("CLIENT"));
    }
}
