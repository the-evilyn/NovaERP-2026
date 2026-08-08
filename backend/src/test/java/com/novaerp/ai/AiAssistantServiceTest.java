package com.novaerp.ai;

import com.novaerp.ai.dto.AiAnomalyDTO;
import com.novaerp.ai.dto.AiChatRequestDTO;
import com.novaerp.ai.dto.AiMessageDTO;
import com.novaerp.ai.dto.AiPredictionDTO;
import com.novaerp.ai.entity.AiAnomaly;
import com.novaerp.ai.entity.AnomalySeverity;
import com.novaerp.ai.entity.AnomalyStatus;
import com.novaerp.ai.entity.AnomalyType;
import com.novaerp.ai.repository.AiAnomalyRepository;
import com.novaerp.ai.service.AiAssistantServiceImpl;
import com.novaerp.invoice.repository.InvoiceRepository;
import com.novaerp.product.entity.Product;
import com.novaerp.product.repository.ProductRepository;
import com.novaerp.sale.entity.SaleStatus;
import com.novaerp.sale.entity.SalesOrder;
import com.novaerp.sale.repository.SalesOrderRepository;
import com.novaerp.stock.entity.Stock;
import com.novaerp.stock.repository.StockRepository;
import com.novaerp.supplier.entity.Supplier;
import com.novaerp.supplier.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiAssistantServiceTest {

    @Mock
    private AiAnomalyRepository aiAnomalyRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private StockRepository stockRepository;
    @Mock
    private SalesOrderRepository salesOrderRepository;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private AiAssistantServiceImpl aiAssistantService;

    @Test
    void testChatSalesQuery() {
        SalesOrder sale = SalesOrder.builder().totalAmount(BigDecimal.valueOf(10000.0)).status(SaleStatus.COMMANDE).build();
        when(salesOrderRepository.findAll()).thenReturn(List.of(sale));

        AiChatRequestDTO req = AiChatRequestDTO.builder().content("Quel est le montant des ventes ?").build();
        AiMessageDTO response = aiAssistantService.chat(req);

        assertNotNull(response);
        assertEquals("assistant", response.getRole());
        assertTrue(response.getContent().contains("10000.00 MAD"));
        assertEquals("SALES_QUERY", response.getMetadata().getIntent());
    }

    @Test
    void testGetStockPredictions() {
        Product prod = Product.builder().id(1L).name("Riz 5kg").sku("RIZ-005").minStockLevel(BigDecimal.valueOf(50)).purchasePrice(BigDecimal.valueOf(45.0)).build();
        Stock stock = Stock.builder().product(prod).quantityAvailable(BigDecimal.valueOf(10)).build();
        Supplier supplier = Supplier.builder().id(1L).name("Agro Supply").build();

        when(stockRepository.findAll()).thenReturn(List.of(stock));
        when(supplierRepository.findAll()).thenReturn(List.of(supplier));

        List<AiPredictionDTO> predictions = aiAssistantService.getStockPredictions();

        assertNotNull(predictions);
        assertEquals(1, predictions.size());
        assertEquals("COMMANDER_URGENT", predictions.get(0).getRecommandation());
        assertEquals("Agro Supply", predictions.get(0).getFournisseurSuggere().getNom());
    }

    @Test
    void testGetAnomaliesAndResolve() {
        AiAnomaly anomaly = AiAnomaly.builder()
                .id(1L)
                .type(AnomalyType.PRIX_ANORMAL)
                .severity(AnomalySeverity.ELEVEE)
                .title("Écart de prix")
                .description("Prix élevé détecté")
                .status(AnomalyStatus.NOUVEAU)
                .detectionDate(LocalDateTime.now())
                .build();

        when(aiAnomalyRepository.findAllByOrderByDetectionDateDesc()).thenReturn(List.of(anomaly));
        when(aiAnomalyRepository.findById(1L)).thenReturn(Optional.of(anomaly));

        List<AiAnomalyDTO> anomalies = aiAssistantService.getAnomalies();
        assertEquals(1, anomalies.size());

        aiAssistantService.resolveAnomaly(1L, AnomalyStatus.RESOLU);
        assertEquals(AnomalyStatus.RESOLU, anomaly.getStatus());
        verify(aiAnomalyRepository, times(1)).save(anomaly);
    }
}
