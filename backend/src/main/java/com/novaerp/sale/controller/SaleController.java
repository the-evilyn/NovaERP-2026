package com.novaerp.sale.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novaerp.sale.dto.SaleDTO;
import com.novaerp.sale.service.SaleService;
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
@RequestMapping("/sales")
@Tag(name = "Sales Management", description = "Endpoints for quotations, customer orders, stock reservation, and dispatch delivery")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class SaleController {

    private final SaleService saleService;
    private final ObjectMapper objectMapper;

    @GetMapping
    @Operation(summary = "List sales orders", description = "Retrieves paginated customer orders and quotations")
    public ResponseEntity<Page<SaleDTO>> getSales(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String search
    ) {
        Sort sort = "asc".equalsIgnoreCase(direction) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SaleDTO> sales = saleService.getSales(pageable, search);
        return ResponseEntity.ok(sales);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sales order by ID", description = "Retrieves sales order with itemized breakdown")
    public ResponseEntity<SaleDTO> getSale(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.getSaleById(id));
    }

    @PostMapping
    @Operation(summary = "Create sales order or batch create", description = "Accepts single sales order or array")
    public ResponseEntity<Object> createSales(
            @RequestBody JsonNode requestNode
    ) throws Exception {
        if (requestNode.isArray()) {
            List<SaleDTO> dtos = objectMapper.readerFor(new TypeReference<List<SaleDTO>>() {}).readValue(requestNode);
            List<SaleDTO> created = saleService.createSales(dtos);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } else {
            SaleDTO dto = objectMapper.treeToValue(requestNode, SaleDTO.class);
            SaleDTO created = saleService.createSale(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update sales order", description = "Updates order status, items, or delivery date")
    public ResponseEntity<SaleDTO> updateSale(
            @PathVariable Long id,
            @Valid @RequestBody SaleDTO dto
    ) {
        return ResponseEntity.ok(saleService.updateSale(id, dto));
    }

    @PostMapping("/{id}/deliver")
    @Operation(summary = "Deliver sales order", description = "Dispatches products and updates inventory counts")
    public ResponseEntity<SaleDTO> deliverSale(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.deliverSale(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete sales order", description = "Deletes order record")
    public ResponseEntity<Void> deleteSale(@PathVariable Long id) {
        saleService.deleteSale(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Batch delete sales", description = "Deletes multiple sales orders by ID array")
    public ResponseEntity<Void> deleteSales(
            @RequestBody(required = false) List<Long> bodyIds,
            @RequestParam(required = false) List<Long> ids
    ) {
        List<Long> targetIds = bodyIds != null ? bodyIds : (ids != null ? ids : Collections.emptyList());
        if (!targetIds.isEmpty()) {
            saleService.deleteSales(targetIds);
        }
        return ResponseEntity.noContent().build();
    }
}
