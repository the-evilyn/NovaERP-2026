package com.novaerp.invoice;

import com.novaerp.client.entity.Client;
import com.novaerp.client.repository.ClientRepository;
import com.novaerp.invoice.dto.InvoiceDTO;
import com.novaerp.invoice.dto.InvoiceItemDTO;
import com.novaerp.invoice.entity.Invoice;
import com.novaerp.invoice.entity.InvoiceItem;
import com.novaerp.invoice.entity.InvoiceStatus;
import com.novaerp.invoice.repository.InvoiceRepository;
import com.novaerp.invoice.service.InvoiceServiceImpl;
import com.novaerp.product.entity.Product;
import com.novaerp.product.repository.ProductRepository;
import com.novaerp.sale.repository.SalesOrderRepository;
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
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    private Client sampleClient;
    private Product sampleProduct;
    private Invoice sampleInvoice;

    @BeforeEach
    void setUp() {
        sampleClient = Client.builder().id(1L).code("CLI-0001").name("LabelVie SA").build();
        sampleProduct = Product.builder().id(1L).name("Huile 5L").sku("HUI-005").purchasePrice(BigDecimal.valueOf(85.0)).sellingPrice(BigDecimal.valueOf(115.0)).build();

        sampleInvoice = Invoice.builder()
                .id(1L)
                .invoiceNumber("FAC-2026-001")
                .client(sampleClient)
                .status(InvoiceStatus.VALIDEE)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .subtotal(BigDecimal.valueOf(11500.0))
                .taxRate(BigDecimal.valueOf(20.0))
                .taxAmount(BigDecimal.valueOf(2300.0))
                .totalAmount(BigDecimal.valueOf(13800.0))
                .paidAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        InvoiceItem item = InvoiceItem.builder()
                .id(1L)
                .invoice(sampleInvoice)
                .product(sampleProduct)
                .quantity(BigDecimal.valueOf(100))
                .unitPrice(BigDecimal.valueOf(115.0))
                .subtotal(BigDecimal.valueOf(11500.0))
                .taxRate(BigDecimal.valueOf(20.0))
                .taxAmount(BigDecimal.valueOf(2300.0))
                .totalAmount(BigDecimal.valueOf(13800.0))
                .build();
        sampleInvoice.getItems().add(item);
    }

    @Test
    void testGetInvoices() {
        Page<Invoice> page = new PageImpl<>(List.of(sampleInvoice));
        when(invoiceRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<InvoiceDTO> result = invoiceService.getInvoices(PageRequest.of(0, 10), null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("FAC-2026-001", result.getContent().get(0).getReference());
    }

    @Test
    void testCreateInvoice() {
        InvoiceDTO input = InvoiceDTO.builder()
                .clientId(1L)
                .lignes(List.of(
                        InvoiceItemDTO.builder()
                                .productId(1L)
                                .quantite(BigDecimal.valueOf(10))
                                .prixUnitaire(BigDecimal.valueOf(115.0))
                                .build()
                ))
                .build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(sampleClient));
        when(invoiceRepository.count()).thenReturn(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> {
            Invoice inv = i.getArgument(0);
            inv.setId(2L);
            return inv;
        });

        InvoiceDTO result = invoiceService.createInvoice(input);

        assertNotNull(result);
        assertEquals(InvoiceStatus.VALIDEE, result.getStatut());
        assertEquals(BigDecimal.valueOf(1150.00).setScale(2), result.getTotalHT().setScale(2));
        assertEquals(BigDecimal.valueOf(1380.00).setScale(2), result.getTotalTTC().setScale(2));
    }
}
