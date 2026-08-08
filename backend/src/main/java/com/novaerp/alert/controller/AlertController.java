package com.novaerp.alert.controller;

import com.novaerp.alert.dto.AlertDTO;
import com.novaerp.alert.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alerts")
@Tag(name = "Alerts & Notifications", description = "Endpoints for critical stock warnings, payment reminders, and system events")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    @Operation(summary = "Get active alerts", description = "Retrieves list of alerts optionally filtered by unread status")
    public ResponseEntity<List<AlertDTO>> getAlerts(
            @RequestParam(required = false, defaultValue = "false") boolean unreadOnly
    ) {
        return ResponseEntity.ok(alertService.getAlerts(unreadOnly));
    }

    @GetMapping("/page")
    @Operation(summary = "Get paginated alerts", description = "Retrieves paginated alerts with sorting")
    public ResponseEntity<Page<AlertDTO>> getAlertsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean unreadOnly
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(alertService.getAlertsPaginated(pageable, unreadOnly));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get alert by ID", description = "Retrieves specific alert details")
    public ResponseEntity<AlertDTO> getAlert(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.getAlertById(id));
    }

    @PostMapping
    @Operation(summary = "Create system alert", description = "Triggers a new operational alert notification")
    public ResponseEntity<AlertDTO> createAlert(@Valid @RequestBody AlertDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alertService.createAlert(dto));
    }

    @RequestMapping(value = "/{id}/read", method = {RequestMethod.PATCH, RequestMethod.PUT})
    @Operation(summary = "Mark alert as read", description = "Flags an individual alert as acknowledged")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        alertService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/mark-all-read", method = {RequestMethod.POST, RequestMethod.PATCH})
    @Operation(summary = "Mark all alerts as read", description = "Clears unread state across all active notifications")
    public ResponseEntity<Void> markAllAsRead() {
        alertService.markAllAsRead();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete alert", description = "Removes alert record")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id) {
        alertService.deleteAlert(id);
        return ResponseEntity.noContent().build();
    }
}
