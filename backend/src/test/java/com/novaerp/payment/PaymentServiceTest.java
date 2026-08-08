package com.novaerp.payment;

import com.novaerp.client.entity.Client;
import com.novaerp.client.repository.ClientRepository;
import com.novaerp.invoice.entity.Invoice;
import com.novaerp.invoice.entity.InvoiceStatus;
import com.novaerp.invoice.repository.InvoiceRepository;
import com.novaerp.payment.dto.PaymentDTO;
import com.novaerp.payment.entity.Payment;
import com.novaerp.payment.entity.PaymentMethod;
import com.novaerp.payment.entity.PaymentStatus;
import com.novaerp.payment.entity.PaymentType;
import com.novaerp.payment.repository.PaymentRepository;
import com.novaerp.payment.service.PaymentServiceImpl;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Client sampleClient;
    private Invoice sampleInvoice;
    private Payment samplePayment;

    @BeforeEach
    void setUp() {
        sampleClient = Client.builder().id(1L).code("CLI-0001").name("LabelVie SA").build();

        sampleInvoice = Invoice.builder()
                .id(1L)
                .invoiceNumber("FAC-2026-001")
                .client(sampleClient)
                .status(InvoiceStatus.VALIDEE)
                .totalAmount(BigDecimal.valueOf(27600.0))
                .paidAmount(BigDecimal.ZERO)
                .build();

        samplePayment = Payment.builder()
                .id(1L)
                .paymentNumber("REG-2026-001")
                .client(sampleClient)
                .invoice(sampleInvoice)
                .type(PaymentType.INBOUND_CUSTOMER)
                .method(PaymentMethod.VIREMENT)
                .status(PaymentStatus.VALIDE)
                .amount(BigDecimal.valueOf(27600.0))
                .paymentDate(LocalDate.now())
                .build();
    }

    @Test
    void testGetPayments() {
        Page<Payment> page = new PageImpl<>(List.of(samplePayment));
        when(paymentRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<PaymentDTO> result = paymentService.getPayments(PageRequest.of(0, 10), null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("REG-2026-001", result.getContent().get(0).getReference());
    }

    @Test
    void testCreatePaymentUpdatesInvoiceStatus() {
        PaymentDTO input = PaymentDTO.builder()
                .factureId(1L)
                .montant(BigDecimal.valueOf(27600.0))
                .modePaiement(PaymentMethod.VIREMENT)
                .build();

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(sampleInvoice));
        when(paymentRepository.count()).thenReturn(1L);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(2L);
            return p;
        });

        PaymentDTO result = paymentService.createPayment(input);

        assertNotNull(result);
        assertEquals(PaymentStatus.VALIDE, result.getStatut());
        assertEquals(BigDecimal.valueOf(27600.0), sampleInvoice.getPaidAmount());
        assertEquals(InvoiceStatus.PAYEE, sampleInvoice.getStatus());
        verify(invoiceRepository, times(1)).save(sampleInvoice);
    }
}
