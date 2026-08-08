package com.novaerp.audit.service;

import com.novaerp.audit.dto.AuditLogDTO;
import com.novaerp.audit.entity.AuditAction;
import com.novaerp.audit.entity.AuditLog;
import com.novaerp.audit.repository.AuditLogRepository;
import com.novaerp.exception.ResourceNotFoundException;
import com.novaerp.security.service.UserPrincipal;
import com.novaerp.user.entity.User;
import com.novaerp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getAuditLogs(Pageable pageable, AuditAction action, String entite, String search) {
        log.info("Fetching audit logs: action={}, entite={}, search={}", action, entite, search);
        return auditLogRepository.searchAuditLogs(action, entite, search, pageable).map(AuditLogDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogDTO getAuditLogById(Long id) {
        AuditLog audit = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog not found with id: " + id));
        return AuditLogDTO.fromEntity(audit);
    }

    @Override
    @Transactional
    public void logAction(AuditAction action, String entityType, Long entityId, String details, String ipAddress) {
        log.info("Recording audit log: action={}, entityType={}, entityId={}", action, entityType, entityId);

        User currentUser = null;
        String userName = "SYSTEM";

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
            currentUser = userRepository.findById(principal.getId()).orElse(null);
            if (currentUser != null) {
                userName = currentUser.getFirstName() + " " + currentUser.getLastName();
            }
        }

        AuditLog logEntry = AuditLog.builder()
                .user(currentUser)
                .userName(userName)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .ipAddress(ipAddress)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(logEntry);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportAuditLogsCsv() {
        log.info("Exporting audit logs to CSV");
        List<AuditLog> list = auditLogRepository.findAllByOrderByTimestampDesc();

        StringBuilder sb = new StringBuilder();
        sb.append("ID,Date,Utilisateur,Action,Entite,EntiteId,Details,IP\n");

        for (AuditLog a : list) {
            sb.append(a.getId()).append(",")
              .append(a.getTimestamp()).append(",")
              .append("\"").append(a.getUserName() != null ? a.getUserName() : "").append("\",")
              .append(a.getAction()).append(",")
              .append(a.getEntityType()).append(",")
              .append(a.getEntityId() != null ? a.getEntityId() : "").append(",")
              .append("\"").append(a.getDetails() != null ? a.getDetails().replace("\"", "\"\"") : "").append("\",")
              .append(a.getIpAddress() != null ? a.getIpAddress() : "").append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}
