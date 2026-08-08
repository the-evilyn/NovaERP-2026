package com.novaerp.audit.service;

import com.novaerp.audit.dto.AuditLogDTO;
import com.novaerp.audit.entity.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {
    Page<AuditLogDTO> getAuditLogs(Pageable pageable, AuditAction action, String entite, String search);
    AuditLogDTO getAuditLogById(Long id);
    void logAction(AuditAction action, String entityType, Long entityId, String details, String ipAddress);
    byte[] exportAuditLogsCsv();
}
