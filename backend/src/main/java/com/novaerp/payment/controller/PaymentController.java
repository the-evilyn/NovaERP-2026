package com.novaerp.payment.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novaerp.payment.dto.PaymentDTO;
import com.novaerp.payment.service.PaymentService;
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

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payments Management", description = "Endpoints for bank transfers, cash collections, checks, and reconciliation")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @GetMapping
    @Operation(summary = "List payments", description = "Retrieves paginated payments and settlements")
    public ResponseEntity<Page<PaymentDTO>> getPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String search
    ) {
        Sort sort = "asc".equalsIgnoreCase(direction) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PaymentDTO> payments = paymentService.getPayments(pageable, search);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Retrieves payment details and invoice linkage")
    public ResponseEntity<PaymentDTO> getPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @PostMapping
    @Operation(summary = "Record payment or batch record", description = "Accepts single payment or array")
    public ResponseEntity<Object> createPayments(
            @RequestBody JsonNode requestNode
    ) throws Exception {
        if (requestNode.isArray()) {
            List<PaymentDTO> dtos = objectMapper.readerFor(new TypeReference<List<PaymentDTO>>() {}).readValue(requestNode);
            List<PaymentDTO> created = paymentService.createPayments(dtos);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } else {
            PaymentDTO dto = objectMapper.treeToValue(requestNode, PaymentDTO.class);
            PaymentDTO created = paymentService.createPayment(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update payment", description = "Updates payment status, method, or notes")
    public ResponseEntity<PaymentDTO> updatePayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentDTO dto
    ) {
        return ResponseEntity.ok(paymentService.updatePayment(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete payment", description = "Deletes payment record and updates invoice balance")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Batch delete payments", description = "Deletes multiple payments by ID array")
    public ResponseEntity<Void> deletePayments(
            @RequestBody(required = false) List<Long> bodyIds,
            @RequestParam(required = false) List<Long> ids
    ) {
        List<Long> targetIds = bodyIds != null ? bodyIds : (ids != null ? ids : Collections.emptyList());
        if (!targetIds.isEmpty()) {
            paymentService.deletePayments(targetIds);
        }
        return ResponseEntity.noContent().build();
    }
}
