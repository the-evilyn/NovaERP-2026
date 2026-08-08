package com.novaerp.audit.controller;

import com.novaerp.audit.dto.AuditLogDTO;
import com.novaerp.audit.entity.AuditAction;
import com.novaerp.audit.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/audit-logs")
@Tag(name = "Audit Logs & Security Tracing", description = "Endpoints for enterprise compliance, tracking user operations, and CSV exports")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "Get paginated audit logs", description = "Filterable by action type, entity, and text search")
    public ResponseEntity<Page<AuditLogDTO>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String entite,
            @RequestParam(required = false) String search
    ) {
        Sort sort = "asc".equalsIgnoreCase(direction) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(auditLogService.getAuditLogs(pageable, action, entite, search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get audit log by ID", description = "Retrieves complete details of an audit entry")
    public ResponseEntity<AuditLogDTO> getAuditLog(@PathVariable Long id) {
        return ResponseEntity.ok(auditLogService.getAuditLogById(id));
    }

    @GetMapping("/export")
    @Operation(summary = "Export audit log trail to CSV", description = "Downloads complete compliance log as CSV")
    public ResponseEntity<byte[]> exportAuditLogs() {
        byte[] csvData = auditLogService.exportAuditLogsCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audit-logs.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}
