package com.novaerp.invoice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novaerp.invoice.dto.InvoiceDTO;
import com.novaerp.invoice.service.InvoiceService;
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
@RequestMapping("/invoices")
@Tag(name = "Invoices Management", description = "Endpoints for fiscal invoicing, VAT breakdowns, payment tracking, and credit notes")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final ObjectMapper objectMapper;

    @GetMapping
    @Operation(summary = "List invoices", description = "Retrieves paginated invoices with customer and payment progress")
    public ResponseEntity<Page<InvoiceDTO>> getInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String search
    ) {
        Sort sort = "asc".equalsIgnoreCase(direction) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<InvoiceDTO> invoices = invoiceService.getInvoices(pageable, search);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get invoice by ID", description = "Retrieves complete invoice with line items")
    public ResponseEntity<InvoiceDTO> getInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    @PostMapping
    @Operation(summary = "Create invoice or batch create", description = "Accepts single invoice or array")
    public ResponseEntity<Object> createInvoices(
            @RequestBody JsonNode requestNode
    ) throws Exception {
        if (requestNode.isArray()) {
            List<InvoiceDTO> dtos = objectMapper.readerFor(new TypeReference<List<InvoiceDTO>>() {}).readValue(requestNode);
            List<InvoiceDTO> created = invoiceService.createInvoices(dtos);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } else {
            InvoiceDTO dto = objectMapper.treeToValue(requestNode, InvoiceDTO.class);
            InvoiceDTO created = invoiceService.createInvoice(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }
    }

    @PostMapping("/from-sale/{saleId}")
    @Operation(summary = "Generate invoice from sales order", description = "Converts an approved sales order directly into a tax invoice")
    public ResponseEntity<InvoiceDTO> createInvoiceFromSale(@PathVariable Long saleId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.createInvoiceFromSale(saleId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update invoice", description = "Updates invoice details and line items")
    public ResponseEntity<InvoiceDTO> updateInvoice(
            @PathVariable Long id,
            @Valid @RequestBody InvoiceDTO dto
    ) {
        return ResponseEntity.ok(invoiceService.updateInvoice(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete invoice", description = "Deletes invoice record")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Batch delete invoices", description = "Deletes multiple invoices by ID array")
    public ResponseEntity<Void> deleteInvoices(
            @RequestBody(required = false) List<Long> bodyIds,
            @RequestParam(required = false) List<Long> ids
    ) {
        List<Long> targetIds = bodyIds != null ? bodyIds : (ids != null ? ids : Collections.emptyList());
        if (!targetIds.isEmpty()) {
            invoiceService.deleteInvoices(targetIds);
        }
        return ResponseEntity.noContent().build();
    }
}
