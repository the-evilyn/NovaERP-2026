package com.novaerp.supplier.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novaerp.supplier.dto.SupplierDTO;
import com.novaerp.supplier.service.SupplierService;
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
@RequestMapping("/suppliers")
@Tag(name = "Suppliers Management", description = "Endpoints for vendor master data, procurement contacts, and ICE identification")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class SupplierController {

    private final SupplierService supplierService;
    private final ObjectMapper objectMapper;

    @GetMapping
    @Operation(summary = "List paginated suppliers", description = "Retrieves suppliers matching search filters with pagination")
    public ResponseEntity<Page<SupplierDTO>> getSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String search
    ) {
        Sort sort = "asc".equalsIgnoreCase(direction) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SupplierDTO> suppliers = supplierService.getSuppliers(pageable, search);
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier by ID", description = "Retrieves single supplier information")
    public ResponseEntity<SupplierDTO> getSupplier(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    @PostMapping
    @Operation(summary = "Create supplier or batch create suppliers", description = "Accepts a single supplier or an array of suppliers")
    public ResponseEntity<Object> createSuppliers(
            @RequestBody JsonNode requestNode
    ) throws Exception {
        if (requestNode.isArray()) {
            List<SupplierDTO> dtos = objectMapper.readerFor(new TypeReference<List<SupplierDTO>>() {}).readValue(requestNode);
            List<SupplierDTO> created = supplierService.createSuppliers(dtos);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } else {
            SupplierDTO dto = objectMapper.treeToValue(requestNode, SupplierDTO.class);
            SupplierDTO created = supplierService.createSupplier(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update supplier", description = "Updates supplier details")
    public ResponseEntity<SupplierDTO> updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody SupplierDTO dto
    ) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete supplier by ID", description = "Deletes supplier record")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Batch delete suppliers", description = "Deletes multiple suppliers by ID array")
    public ResponseEntity<Void> deleteSuppliers(
            @RequestBody(required = false) List<Long> bodyIds,
            @RequestParam(required = false) List<Long> ids
    ) {
        List<Long> targetIds = bodyIds != null ? bodyIds : (ids != null ? ids : Collections.emptyList());
        if (!targetIds.isEmpty()) {
            supplierService.deleteSuppliers(targetIds);
        }
        return ResponseEntity.noContent().build();
    }
}
