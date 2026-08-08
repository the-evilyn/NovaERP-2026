package com.novaerp.purchase;

import com.novaerp.exception.BusinessRuleException;
import com.novaerp.product.entity.Product;
import com.novaerp.product.repository.ProductRepository;
import com.novaerp.purchase.dto.PurchaseDTO;
import com.novaerp.purchase.dto.PurchaseItemDTO;
import com.novaerp.purchase.entity.PurchaseOrder;
import com.novaerp.purchase.entity.PurchaseOrderItem;
import com.novaerp.purchase.entity.PurchaseStatus;
import com.novaerp.purchase.repository.PurchaseOrderRepository;
import com.novaerp.purchase.service.PurchaseServiceImpl;
import com.novaerp.stock.entity.Stock;
import com.novaerp.stock.entity.Warehouse;
import com.novaerp.stock.repository.StockMovementRepository;
import com.novaerp.stock.repository.StockRepository;
import com.novaerp.stock.repository.WarehouseRepository;
import com.novaerp.supplier.entity.Supplier;
import com.novaerp.supplier.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private PurchaseServiceImpl purchaseService;

    private Supplier sampleSupplier;
    private Product sampleProduct;
    private Warehouse sampleWarehouse;
    private PurchaseOrder sampleOrder;

    @BeforeEach
    void setUp() {
        sampleSupplier = Supplier.builder().id(1L).code("FRN-0001").name("Huileries du Souss").build();
        sampleProduct = Product.builder().id(1L).name("Huile 5L").sku("HUI-005").purchasePrice(BigDecimal.valueOf(85.0)).build();
        sampleWarehouse = Warehouse.builder().id(1L).code("WH-01").name("Casablanca").build();

        sampleOrder = PurchaseOrder.builder()
                .id(1L)
                .orderNumber("ACH-2026-001")
                .supplier(sampleSupplier)
                .warehouse(sampleWarehouse)
                .status(PurchaseStatus.EN_ATTENTE)
                .orderDate(LocalDate.now())
                .subtotal(BigDecimal.valueOf(8500.0))
                .taxRate(BigDecimal.valueOf(20.0))
                .taxAmount(BigDecimal.valueOf(1700.0))
                .totalAmount(BigDecimal.valueOf(10200.0))
                .items(new ArrayList<>())
                .build();

        PurchaseOrderItem item = PurchaseOrderItem.builder()
                .id(1L)
                .purchaseOrder(sampleOrder)
                .product(sampleProduct)
                .quantityOrdered(BigDecimal.valueOf(100))
                .unitPrice(BigDecimal.valueOf(85.0))
                .subtotal(BigDecimal.valueOf(8500.0))
                .taxRate(BigDecimal.valueOf(20.0))
                .taxAmount(BigDecimal.valueOf(1700.0))
                .totalAmount(BigDecimal.valueOf(10200.0))
                .build();
        sampleOrder.getItems().add(item);
    }

    @Test
    void testGetPurchases() {
        Page<PurchaseOrder> page = new PageImpl<>(List.of(sampleOrder));
        when(purchaseOrderRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<PurchaseDTO> result = purchaseService.getPurchases(PageRequest.of(0, 10), null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("ACH-2026-001", result.getContent().get(0).getReference());
    }

    @Test
    void testCreatePurchase() {
        PurchaseDTO input = PurchaseDTO.builder()
                .fournisseurId(1L)
                .items(List.of(
                        PurchaseItemDTO.builder()
                                .produitId(1L)
                                .quantite(BigDecimal.valueOf(50))
                                .prixUnitaire(BigDecimal.valueOf(85.0))
                                .build()
                ))
                .build();

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(sampleSupplier));
        when(warehouseRepository.findAll()).thenReturn(List.of(sampleWarehouse));
        when(purchaseOrderRepository.count()).thenReturn(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(i -> {
            PurchaseOrder po = i.getArgument(0);
            po.setId(2L);
            return po;
        });

        PurchaseDTO result = purchaseService.createPurchase(input);

        assertNotNull(result);
        assertEquals(PurchaseStatus.EN_ATTENTE, result.getStatut());
        assertEquals(BigDecimal.valueOf(4250.00).setScale(2), result.getTotalHT().setScale(2));
    }

    @Test
    void testReceivePurchaseIncrementsStock() {
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(stockRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(
                Stock.builder().id(1L).product(sampleProduct).warehouse(sampleWarehouse).quantityOnHand(BigDecimal.valueOf(20)).quantityAllocated(BigDecimal.ZERO).quantityAvailable(BigDecimal.valueOf(20)).build()
        ));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(sampleOrder);

        PurchaseDTO result = purchaseService.receivePurchase(1L);

        assertNotNull(result);
        assertEquals(PurchaseStatus.RECUE, result.getStatut());
        verify(stockRepository, times(1)).save(any(Stock.class));
        verify(stockMovementRepository, times(1)).save(any());
    }
}
