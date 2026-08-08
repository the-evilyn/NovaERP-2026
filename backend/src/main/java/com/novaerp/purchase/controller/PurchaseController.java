package com.novaerp.purchase.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novaerp.purchase.dto.PurchaseDTO;
import com.novaerp.purchase.service.PurchaseService;
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
@RequestMapping("/purchases")
@Tag(name = "Purchases Management", description = "Endpoints for procurement orders, line item calculations, and supplier goods receipt")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final ObjectMapper objectMapper;

    @GetMapping
    @Operation(summary = "List purchase orders", description = "Retrieves paginated procurement orders with supplier and total info")
    public ResponseEntity<Page<PurchaseDTO>> getPurchases(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String search
    ) {
        Sort sort = "asc".equalsIgnoreCase(direction) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PurchaseDTO> purchases = purchaseService.getPurchases(pageable, search);
        return ResponseEntity.ok(purchases);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purchase order by ID", description = "Retrieves purchase order with all items")
    public ResponseEntity<PurchaseDTO> getPurchase(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.getPurchaseById(id));
    }

    @PostMapping
    @Operation(summary = "Create purchase order or batch create", description = "Accepts single purchase order or array")
    public ResponseEntity<Object> createPurchases(
            @RequestBody JsonNode requestNode
    ) throws Exception {
        if (requestNode.isArray()) {
            List<PurchaseDTO> dtos = objectMapper.readerFor(new TypeReference<List<PurchaseDTO>>() {}).readValue(requestNode);
            List<PurchaseDTO> created = purchaseService.createPurchases(dtos);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } else {
            PurchaseDTO dto = objectMapper.treeToValue(requestNode, PurchaseDTO.class);
            PurchaseDTO created = purchaseService.createPurchase(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update purchase order", description = "Updates procurement details and items")
    public ResponseEntity<PurchaseDTO> updatePurchase(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseDTO dto
    ) {
        return ResponseEntity.ok(purchaseService.updatePurchase(id, dto));
    }

    @PostMapping("/{id}/receive")
    @Operation(summary = "Receive purchase goods", description = "Changes status to RECUE and automatically increments warehouse inventory")
    public ResponseEntity<PurchaseDTO> receivePurchase(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.receivePurchase(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete purchase order", description = "Deletes procurement record")
    public ResponseEntity<Void> deletePurchase(@PathVariable Long id) {
        purchaseService.deletePurchase(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Batch delete purchases", description = "Deletes multiple purchase orders by ID array")
    public ResponseEntity<Void> deletePurchases(
            @RequestBody(required = false) List<Long> bodyIds,
            @RequestParam(required = false) List<Long> ids
    ) {
        List<Long> targetIds = bodyIds != null ? bodyIds : (ids != null ? ids : Collections.emptyList());
        if (!targetIds.isEmpty()) {
            purchaseService.deletePurchases(targetIds);
        }
        return ResponseEntity.noContent().build();
    }
}
