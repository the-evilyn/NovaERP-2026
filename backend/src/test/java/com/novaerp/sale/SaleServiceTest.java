package com.novaerp.sale;

import com.novaerp.client.entity.Client;
import com.novaerp.client.repository.ClientRepository;
import com.novaerp.exception.BusinessRuleException;
import com.novaerp.product.entity.Product;
import com.novaerp.product.repository.ProductRepository;
import com.novaerp.sale.dto.SaleDTO;
import com.novaerp.sale.dto.SaleItemDTO;
import com.novaerp.sale.entity.SaleStatus;
import com.novaerp.sale.entity.SalesOrder;
import com.novaerp.sale.entity.SalesOrderItem;
import com.novaerp.sale.repository.SalesOrderRepository;
import com.novaerp.sale.service.SaleServiceImpl;
import com.novaerp.stock.entity.Stock;
import com.novaerp.stock.entity.Warehouse;
import com.novaerp.stock.repository.StockMovementRepository;
import com.novaerp.stock.repository.StockRepository;
import com.novaerp.stock.repository.WarehouseRepository;
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
class SaleServiceTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private SaleServiceImpl saleService;

    private Client sampleClient;
    private Product sampleProduct;
    private Warehouse sampleWarehouse;
    private SalesOrder sampleOrder;

    @BeforeEach
    void setUp() {
        sampleClient = Client.builder().id(1L).code("CLI-0001").name("LabelVie SA").build();
        sampleProduct = Product.builder().id(1L).name("Huile 5L").sku("HUI-005").purchasePrice(BigDecimal.valueOf(85.0)).salePrice(BigDecimal.valueOf(115.0)).build();
        sampleWarehouse = Warehouse.builder().id(1L).code("WH-01").name("Casablanca").build();

        sampleOrder = SalesOrder.builder()
                .id(1L)
                .orderNumber("VTE-2026-001")
                .client(sampleClient)
                .warehouse(sampleWarehouse)
                .status(SaleStatus.COMMANDE)
                .orderDate(LocalDate.now())
                .subtotal(BigDecimal.valueOf(11500.0))
                .taxRate(BigDecimal.valueOf(20.0))
                .taxAmount(BigDecimal.valueOf(2300.0))
                .totalAmount(BigDecimal.valueOf(13800.0))
                .items(new ArrayList<>())
                .build();

        SalesOrderItem item = SalesOrderItem.builder()
                .id(1L)
                .salesOrder(sampleOrder)
                .product(sampleProduct)
                .quantityOrdered(BigDecimal.valueOf(100))
                .unitPrice(BigDecimal.valueOf(115.0))
                .subtotal(BigDecimal.valueOf(11500.0))
                .taxRate(BigDecimal.valueOf(20.0))
                .taxAmount(BigDecimal.valueOf(2300.0))
                .totalAmount(BigDecimal.valueOf(13800.0))
                .build();
        sampleOrder.getItems().add(item);
    }

    @Test
    void testGetSales() {
        Page<SalesOrder> page = new PageImpl<>(List.of(sampleOrder));
        when(salesOrderRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<SaleDTO> result = saleService.getSales(PageRequest.of(0, 10), null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("VTE-2026-001", result.getContent().get(0).getReference());
    }

    @Test
    void testCreateSale() {
        SaleDTO input = SaleDTO.builder()
                .clientId(1L)
                .items(List.of(
                        SaleItemDTO.builder()
                                .produitId(1L)
                                .quantite(BigDecimal.valueOf(50))
                                .prixUnitaire(BigDecimal.valueOf(115.0))
                                .build()
                ))
                .build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(sampleClient));
        when(warehouseRepository.findAll()).thenReturn(List.of(sampleWarehouse));
        when(salesOrderRepository.count()).thenReturn(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenAnswer(i -> {
            SalesOrder so = i.getArgument(0);
            so.setId(2L);
            return so;
        });

        SaleDTO result = saleService.createSale(input);

        assertNotNull(result);
        assertEquals(SaleStatus.COMMANDE, result.getStatut());
        assertEquals(BigDecimal.valueOf(5750.00).setScale(2), result.getTotalHT().setScale(2));
    }

    @Test
    void testDeliverSaleDeductsStock() {
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(stockRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(
                Stock.builder().id(1L).product(sampleProduct).warehouse(sampleWarehouse).quantityOnHand(BigDecimal.valueOf(200)).quantityAllocated(BigDecimal.ZERO).quantityAvailable(BigDecimal.valueOf(200)).build()
        ));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(sampleOrder);

        SaleDTO result = saleService.deliverSale(1L);

        assertNotNull(result);
        assertEquals(SaleStatus.LIVREE, result.getStatut());
        verify(stockRepository, times(1)).save(any(Stock.class));
        verify(stockMovementRepository, times(1)).save(any());
    }

    @Test
    void testDeliverSaleInsufficientStockThrowsException() {
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(stockRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(
                Stock.builder().id(1L).product(sampleProduct).warehouse(sampleWarehouse).quantityOnHand(BigDecimal.valueOf(10)).quantityAllocated(BigDecimal.ZERO).quantityAvailable(BigDecimal.valueOf(10)).build()
        ));

        assertThrows(BusinessRuleException.class, () -> saleService.deliverSale(1L));
    }
}
