package com.novaerp.stock;

import com.novaerp.exception.BusinessRuleException;
import com.novaerp.product.entity.Product;
import com.novaerp.product.repository.ProductRepository;
import com.novaerp.stock.dto.StockAdjustmentRequest;
import com.novaerp.stock.dto.StockMovementDTO;
import com.novaerp.stock.dto.StockTransferRequest;
import com.novaerp.stock.entity.Stock;
import com.novaerp.stock.entity.StockMovement;
import com.novaerp.stock.entity.Warehouse;
import com.novaerp.stock.repository.StockMovementRepository;
import com.novaerp.stock.repository.StockRepository;
import com.novaerp.stock.repository.WarehouseRepository;
import com.novaerp.stock.service.StockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private StockServiceImpl stockService;

    private Product sampleProduct;
    private Warehouse sampleWarehouse;
    private Stock sampleStock;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder().id(1L).name("Huile 5L").sku("HUI-005").purchasePrice(BigDecimal.valueOf(85.0)).minStockLevel(BigDecimal.valueOf(30.0)).build();
        sampleWarehouse = Warehouse.builder().id(1L).code("WH-01").name("Casablanca").build();
        sampleStock = Stock.builder().id(1L).product(sampleProduct).warehouse(sampleWarehouse).quantityOnHand(BigDecimal.valueOf(100.0)).quantityAllocated(BigDecimal.ZERO).quantityAvailable(BigDecimal.valueOf(100.0)).build();
    }

    @Test
    void testAdjustStockEntry() {
        StockAdjustmentRequest req = StockAdjustmentRequest.builder()
                .productId(1L)
                .warehouseId(1L)
                .quantity(BigDecimal.valueOf(50.0))
                .type("ENTREE")
                .motif("Réception")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(sampleWarehouse));
        when(stockRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(sampleStock));
        when(stockRepository.save(any(Stock.class))).thenReturn(sampleStock);
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(i -> {
            StockMovement sm = i.getArgument(0);
            sm.setId(10L);
            return sm;
        });

        StockMovementDTO result = stockService.adjustStock(req);

        assertNotNull(result);
        assertEquals("ENTREE", result.getType());
        assertEquals(BigDecimal.valueOf(50.0), result.getQuantite());
        assertEquals(BigDecimal.valueOf(150.0), sampleStock.getQuantityOnHand());
    }

    @Test
    void testAdjustStockExitInsufficientThrowsException() {
        StockAdjustmentRequest req = StockAdjustmentRequest.builder()
                .productId(1L)
                .warehouseId(1L)
                .quantity(BigDecimal.valueOf(200.0))
                .type("SORTIE")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(sampleWarehouse));
        when(stockRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(sampleStock));

        assertThrows(BusinessRuleException.class, () -> stockService.adjustStock(req));
    }

    @Test
    void testTransferStockSameWarehouseThrowsException() {
        StockTransferRequest req = StockTransferRequest.builder()
                .productId(1L)
                .sourceWarehouseId(1L)
                .targetWarehouseId(1L)
                .quantity(BigDecimal.valueOf(10.0))
                .build();

        assertThrows(BusinessRuleException.class, () -> stockService.transferStock(req));
    }
}
