package com.novaerp.stock.controller;

import com.novaerp.common.response.ApiResponse;
import com.novaerp.stock.dto.WarehouseDTO;
import com.novaerp.stock.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/warehouses")
@Tag(name = "Warehouse Locations", description = "Endpoints for managing distribution centres, depots, and storage facilities")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    @Operation(summary = "List warehouses", description = "Retrieves all active warehouses")
    public ResponseEntity<List<WarehouseDTO>> getAllWarehouses() {
        return ResponseEntity.ok(warehouseService.getAllWarehouses());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get warehouse by ID", description = "Retrieves single warehouse details")
    public ResponseEntity<WarehouseDTO> getWarehouse(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.getWarehouseById(id));
    }

    @PostMapping
    @Operation(summary = "Create warehouse", description = "Creates a new warehouse facility")
    public ResponseEntity<ApiResponse<WarehouseDTO>> createWarehouse(
            @Valid @RequestBody WarehouseDTO dto,
            HttpServletRequest request
    ) {
        WarehouseDTO created = warehouseService.createWarehouse(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.created(created, "Warehouse created successfully", request.getRequestURI())
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update warehouse", description = "Updates warehouse details")
    public ResponseEntity<WarehouseDTO> updateWarehouse(
            @PathVariable Long id,
            @Valid @RequestBody WarehouseDTO dto
    ) {
        return ResponseEntity.ok(warehouseService.updateWarehouse(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete warehouse", description = "Deletes a warehouse location")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.noContent().build();
    }
}
