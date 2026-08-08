package com.novaerp.stock.controller;

import com.novaerp.stock.dto.StockAdjustmentRequest;
import com.novaerp.stock.dto.StockDTO;
import com.novaerp.stock.dto.StockMovementDTO;
import com.novaerp.stock.dto.StockTransferRequest;
import com.novaerp.stock.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stock")
@Tag(name = "Stock & Inventory", description = "Endpoints for multi-warehouse stock visibility, inventory corrections, and movement logs")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    @Operation(summary = "Get stock levels", description = "Retrieves all product stock balances across warehouses")
    public ResponseEntity<List<StockDTO>> getAllStock() {
        return ResponseEntity.ok(stockService.getAllStock());
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock alerts", description = "Retrieves items where available stock is less than or equal to minimum threshold")
    public ResponseEntity<List<StockDTO>> getLowStockAlerts() {
        return ResponseEntity.ok(stockService.getLowStockAlerts());
    }

    @GetMapping("/movements")
    @Operation(summary = "Get stock movements history", description = "Retrieves paginated audit log of all stock in/out/adjustments/transfers")
    public ResponseEntity<Page<StockMovementDTO>> getStockMovements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = "asc".equalsIgnoreCase(direction) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(stockService.getStockMovements(pageable));
    }

    @PostMapping("/adjust")
    @Operation(summary = "Adjust stock quantity", description = "Manually adjust or correct inventory balance")
    public ResponseEntity<StockMovementDTO> adjustStock(@Valid @RequestBody StockAdjustmentRequest request) {
        StockMovementDTO result = stockService.adjustStock(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/movements")
    @Operation(summary = "Record stock movement", description = "Endpoint for stock movement creation (alias for adjust)")
    public ResponseEntity<StockMovementDTO> createStockMovement(@Valid @RequestBody StockAdjustmentRequest request) {
        StockMovementDTO result = stockService.adjustStock(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer stock between warehouses", description = "Moves quantity of an SKU from source to target warehouse")
    public ResponseEntity<StockMovementDTO> transferStock(@Valid @RequestBody StockTransferRequest request) {
        StockMovementDTO result = stockService.transferStock(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
