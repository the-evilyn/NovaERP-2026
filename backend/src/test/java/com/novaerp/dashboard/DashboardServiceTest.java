package com.novaerp.dashboard;

import com.novaerp.client.repository.ClientRepository;
import com.novaerp.dashboard.dto.DashboardStatsDTO;
import com.novaerp.dashboard.dto.RecentActivityDTO;
import com.novaerp.dashboard.dto.SalesTrendDTO;
import com.novaerp.dashboard.dto.TopProductDTO;
import com.novaerp.dashboard.service.DashboardServiceImpl;
import com.novaerp.invoice.repository.InvoiceRepository;
import com.novaerp.payment.repository.PaymentRepository;
import com.novaerp.product.entity.Product;
import com.novaerp.product.repository.ProductRepository;
import com.novaerp.purchase.entity.PurchaseOrder;
import com.novaerp.purchase.entity.PurchaseStatus;
import com.novaerp.purchase.repository.PurchaseOrderRepository;
import com.novaerp.sale.entity.SaleStatus;
import com.novaerp.sale.entity.SalesOrder;
import com.novaerp.sale.entity.SalesOrderItem;
import com.novaerp.sale.repository.SalesOrderItemRepository;
import com.novaerp.sale.repository.SalesOrderRepository;
import com.novaerp.stock.entity.Stock;
import com.novaerp.stock.repository.StockRepository;
import com.novaerp.supplier.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;
    @Mock
    private SalesOrderItemRepository salesOrderItemRepository;
    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private StockRepository stockRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void testGetDashboardStats() {
        Product prod = Product.builder().id(1L).purchasePrice(BigDecimal.valueOf(50.0)).minStockLevel(BigDecimal.valueOf(10)).build();
        Stock stock = Stock.builder().product(prod).quantityOnHand(BigDecimal.valueOf(100)).quantityAvailable(BigDecimal.valueOf(100)).build();

        SalesOrder sale = SalesOrder.builder().totalAmount(BigDecimal.valueOf(20000.0)).status(SaleStatus.COMMANDE).orderDate(LocalDate.now()).build();
        PurchaseOrder purchase = PurchaseOrder.builder().totalAmount(BigDecimal.valueOf(15000.0)).status(PurchaseStatus.RECUE).orderDate(LocalDate.now()).build();

        when(salesOrderRepository.findAll()).thenReturn(List.of(sale));
        when(purchaseOrderRepository.findAll()).thenReturn(List.of(purchase));
        when(stockRepository.findAll()).thenReturn(List.of(stock));
        when(clientRepository.count()).thenReturn(5L);
        when(supplierRepository.count()).thenReturn(3L);
        when(invoiceRepository.findAll()).thenReturn(List.of());

        DashboardStatsDTO stats = dashboardService.getDashboardStats();

        assertNotNull(stats);
        assertEquals(BigDecimal.valueOf(20000.0), stats.getTotalVentes());
        assertEquals(BigDecimal.valueOf(15000.0), stats.getTotalAchats());
        assertEquals(BigDecimal.valueOf(5000.0).setScale(2), stats.getValeurStock().setScale(2));
        assertEquals(5L, stats.getTotalClients());
        assertEquals(3L, stats.getTotalFournisseurs());
    }

    @Test
    void testGetSalesTrends() {
        SalesOrder sale = SalesOrder.builder().totalAmount(BigDecimal.valueOf(5000.0)).status(SaleStatus.COMMANDE).orderDate(LocalDate.now()).build();
        PurchaseOrder purchase = PurchaseOrder.builder().totalAmount(BigDecimal.valueOf(3000.0)).status(PurchaseStatus.RECUE).orderDate(LocalDate.now()).build();

        when(salesOrderRepository.findAll()).thenReturn(List.of(sale));
        when(purchaseOrderRepository.findAll()).thenReturn(List.of(purchase));

        List<SalesTrendDTO> trends = dashboardService.getSalesTrends();

        assertNotNull(trends);
        assertEquals(6, trends.size());
    }

    @Test
    void testGetTopProducts() {
        Product prod = Product.builder().id(1L).name("Huile 5L").sku("HUI-005").build();
        SalesOrderItem item = SalesOrderItem.builder().product(prod).quantityOrdered(BigDecimal.valueOf(50)).totalAmount(BigDecimal.valueOf(5750.0)).build();

        when(salesOrderItemRepository.findAll()).thenReturn(List.of(item));

        List<TopProductDTO> topProducts = dashboardService.getTopProducts(5);

        assertNotNull(topProducts);
        assertEquals(1, topProducts.size());
        assertEquals("Huile 5L", topProducts.get(0).getNom());
        assertEquals(BigDecimal.valueOf(5750.0), topProducts.get(0).getChiffreAffaires());
    }
}
