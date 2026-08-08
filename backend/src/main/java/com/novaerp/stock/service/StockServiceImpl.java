package com.novaerp.stock.service;

import com.novaerp.exception.BusinessRuleException;
import com.novaerp.exception.ResourceNotFoundException;
import com.novaerp.product.entity.Product;
import com.novaerp.product.repository.ProductRepository;
import com.novaerp.stock.dto.StockAdjustmentRequest;
import com.novaerp.stock.dto.StockDTO;
import com.novaerp.stock.dto.StockMovementDTO;
import com.novaerp.stock.dto.StockTransferRequest;
import com.novaerp.stock.entity.Stock;
import com.novaerp.stock.entity.StockMovement;
import com.novaerp.stock.entity.StockMovementType;
import com.novaerp.stock.entity.Warehouse;
import com.novaerp.stock.repository.StockMovementRepository;
import com.novaerp.stock.repository.StockRepository;
import com.novaerp.stock.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> getAllStock() {
        return stockRepository.findAll().stream()
                .map(StockDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> getLowStockAlerts() {
        return stockRepository.findLowStockItems().stream()
                .map(StockDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockMovementDTO> getStockMovements(Pageable pageable) {
        return stockMovementRepository.findAllWithDetails(pageable)
                .map(StockMovementDTO::fromEntity);
    }

    @Override
    @Transactional
    public StockMovementDTO adjustStock(StockAdjustmentRequest request) {
        log.info("Processing stock adjustment for product ID: {}", request.getProductId());

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        Warehouse warehouse = request.getWarehouseId() != null
                ? warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + request.getWarehouseId()))
                : warehouseRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No active warehouse found"));

        Stock stock = stockRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId())
                .orElseGet(() -> Stock.builder()
                        .product(product)
                        .warehouse(warehouse)
                        .quantityOnHand(BigDecimal.ZERO)
                        .quantityAllocated(BigDecimal.ZERO)
                        .quantityAvailable(BigDecimal.ZERO)
                        .build());

        BigDecimal adjQty = request.getQuantity();
        String reqType = request.getType() != null ? request.getType().toUpperCase() : "AJUSTEMENT";

        StockMovementType movementType;
        if ("ENTREE".equals(reqType) || "IN".equals(reqType)) {
            movementType = StockMovementType.IN_PURCHASE;
            stock.setQuantityOnHand(stock.getQuantityOnHand().add(adjQty.abs()));
        } else if ("SORTIE".equals(reqType) || "OUT".equals(reqType)) {
            if (stock.getQuantityOnHand().compareTo(adjQty.abs()) < 0) {
                throw new BusinessRuleException("Insufficient stock available for deduction: on hand " + stock.getQuantityOnHand());
            }
            movementType = StockMovementType.OUT_SALE;
            stock.setQuantityOnHand(stock.getQuantityOnHand().subtract(adjQty.abs()));
        } else {
            // Absolute adjustment: new total = adjQty
            BigDecimal diff = adjQty.subtract(stock.getQuantityOnHand());
            movementType = diff.compareTo(BigDecimal.ZERO) >= 0 ? StockMovementType.ADJUSTMENT_IN : StockMovementType.ADJUSTMENT_OUT;
            stock.setQuantityOnHand(adjQty);
        }

        stock.recalculateAvailable();
        stockRepository.save(stock);

        StockMovement movement = StockMovement.builder()
                .product(product)
                .sourceWarehouse(movementType == StockMovementType.OUT_SALE || movementType == StockMovementType.ADJUSTMENT_OUT ? warehouse : null)
                .targetWarehouse(movementType == StockMovementType.IN_PURCHASE || movementType == StockMovementType.ADJUSTMENT_IN ? warehouse : null)
                .movementType(movementType)
                .quantity(adjQty)
                .unitCost(product.getPurchasePrice())
                .referenceType("STOCK_ADJUSTMENT")
                .referenceId("ADJ-" + System.currentTimeMillis())
                .notes(request.getMotif() != null ? request.getMotif() : "Manual stock correction")
                .build();

        StockMovement savedMovement = stockMovementRepository.save(movement);
        return StockMovementDTO.fromEntity(savedMovement);
    }

    @Override
    @Transactional
    public StockMovementDTO transferStock(StockTransferRequest request) {
        log.info("Processing stock transfer of product ID: {} from {} to {}",
                request.getProductId(), request.getSourceWarehouseId(), request.getTargetWarehouseId());

        if (request.getSourceWarehouseId().equals(request.getTargetWarehouseId())) {
            throw new BusinessRuleException("Source and target warehouse cannot be the same");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        Warehouse sourceWh = warehouseRepository.findById(request.getSourceWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Source warehouse not found: " + request.getSourceWarehouseId()));

        Warehouse targetWh = warehouseRepository.findById(request.getTargetWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Target warehouse not found: " + request.getTargetWarehouseId()));

        Stock sourceStock = stockRepository.findByProductIdAndWarehouseId(product.getId(), sourceWh.getId())
                .orElseThrow(() -> new BusinessRuleException("No stock record in source warehouse"));

        if (sourceStock.getQuantityAvailable().compareTo(request.getQuantity()) < 0) {
            throw new BusinessRuleException("Insufficient available stock in source warehouse. Available: " + sourceStock.getQuantityAvailable());
        }

        Stock targetStock = stockRepository.findByProductIdAndWarehouseId(product.getId(), targetWh.getId())
                .orElseGet(() -> Stock.builder()
                        .product(product)
                        .warehouse(targetWh)
                        .quantityOnHand(BigDecimal.ZERO)
                        .quantityAllocated(BigDecimal.ZERO)
                        .quantityAvailable(BigDecimal.ZERO)
                        .build());

        sourceStock.setQuantityOnHand(sourceStock.getQuantityOnHand().subtract(request.getQuantity()));
        sourceStock.recalculateAvailable();
        stockRepository.save(sourceStock);

        targetStock.setQuantityOnHand(targetStock.getQuantityOnHand().add(request.getQuantity()));
        targetStock.recalculateAvailable();
        stockRepository.save(targetStock);

        StockMovement movement = StockMovement.builder()
                .product(product)
                .sourceWarehouse(sourceWh)
                .targetWarehouse(targetWh)
                .movementType(StockMovementType.TRANSFER)
                .quantity(request.getQuantity())
                .unitCost(product.getPurchasePrice())
                .referenceType("INTERNAL_TRANSFER")
                .referenceId("TRF-" + System.currentTimeMillis())
                .notes(request.getNotes() != null ? request.getNotes() : "Warehouse transfer")
                .build();

        StockMovement saved = stockMovementRepository.save(movement);
        return StockMovementDTO.fromEntity(saved);
    }
}
