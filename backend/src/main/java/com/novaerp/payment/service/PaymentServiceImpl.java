package com.novaerp.payment.service;

import com.novaerp.client.entity.Client;
import com.novaerp.client.repository.ClientRepository;
import com.novaerp.exception.ResourceNotFoundException;
import com.novaerp.invoice.entity.Invoice;
import com.novaerp.invoice.repository.InvoiceRepository;
import com.novaerp.payment.dto.PaymentDTO;
import com.novaerp.payment.entity.Payment;
import com.novaerp.payment.entity.PaymentMethod;
import com.novaerp.payment.entity.PaymentStatus;
import com.novaerp.payment.entity.PaymentType;
import com.novaerp.payment.repository.PaymentRepository;
import com.novaerp.supplier.entity.Supplier;
import com.novaerp.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final SupplierRepository supplierRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDTO> getPayments(Pageable pageable, String search) {
        Page<Payment> page = StringUtils.hasText(search)
                ? paymentRepository.searchPayments(search, pageable)
                : paymentRepository.findAll(pageable);
        return page.map(PaymentDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        return PaymentDTO.fromEntity(payment);
    }

    @Override
    @Transactional
    public PaymentDTO createPayment(PaymentDTO dto) {
        log.info("Recording payment for amount: {}", dto.getMontant());

        Invoice invoice = dto.getFactureId() != null
                ? invoiceRepository.findById(dto.getFactureId()).orElse(null)
                : null;

        Client client = null;
        if (dto.getClientId() != null) {
            client = clientRepository.findById(dto.getClientId()).orElse(null);
        } else if (invoice != null) {
            client = invoice.getClient();
        }

        Supplier supplier = dto.getSupplierId() != null
                ? supplierRepository.findById(dto.getSupplierId()).orElse(null)
                : null;

        String reference = StringUtils.hasText(dto.getReference())
                ? dto.getReference().trim()
                : generatePaymentNumber();

        Payment payment = Payment.builder()
                .paymentNumber(reference)
                .client(client)
                .supplier(supplier)
                .invoice(invoice)
                .type(dto.getType() != null ? dto.getType() : PaymentType.INBOUND_CUSTOMER)
                .method(dto.getModePaiement() != null ? dto.getModePaiement() : PaymentMethod.VIREMENT)
                .status(dto.getStatut() != null ? dto.getStatut() : PaymentStatus.VALIDE)
                .amount(dto.getMontant())
                .paymentDate(dto.getDate() != null ? dto.getDate() : LocalDate.now())
                .referenceNumber(dto.getReferenceBancaire())
                .notes(dto.getNotes())
                .build();

        Payment saved = paymentRepository.save(payment);

        if (invoice != null && (saved.getStatus() == PaymentStatus.VALIDE || saved.getStatus() == PaymentStatus.CLEARED)) {
            BigDecimal newPaid = invoice.getPaidAmount() != null ? invoice.getPaidAmount().add(saved.getAmount()) : saved.getAmount();
            invoice.updatePaymentStatus(newPaid);
            invoiceRepository.save(invoice);
        }

        return PaymentDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public List<PaymentDTO> createPayments(List<PaymentDTO> dtos) {
        log.info("Batch recording {} payments", dtos.size());
        List<PaymentDTO> results = new ArrayList<>();
        for (PaymentDTO dto : dtos) {
            results.add(createPayment(dto));
        }
        return results;
    }

    @Override
    @Transactional
    public PaymentDTO updatePayment(Long id, PaymentDTO dto) {
        log.info("Updating payment ID: {}", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        BigDecimal oldAmount = payment.getAmount();
        Invoice invoice = payment.getInvoice();

        if (dto.getMontant() != null) {
            payment.setAmount(dto.getMontant());
        }
        if (dto.getModePaiement() != null) {
            payment.setMethod(dto.getModePaiement());
        }
        if (dto.getStatut() != null) {
            payment.setStatus(dto.getStatut());
        }
        if (dto.getDate() != null) {
            payment.setPaymentDate(dto.getDate());
        }
        if (dto.getReferenceBancaire() != null) {
            payment.setReferenceNumber(dto.getReferenceBancaire());
        }
        if (dto.getNotes() != null) {
            payment.setNotes(dto.getNotes());
        }

        Payment saved = paymentRepository.save(payment);

        if (invoice != null) {
            BigDecimal currentPaid = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO;
            BigDecimal diff = saved.getAmount().subtract(oldAmount);
            invoice.updatePaymentStatus(currentPaid.add(diff));
            invoiceRepository.save(invoice);
        }

        return PaymentDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deletePayment(Long id) {
        log.info("Deleting payment ID: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        Invoice invoice = payment.getInvoice();
        if (invoice != null) {
            BigDecimal currentPaid = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO;
            BigDecimal newPaid = currentPaid.subtract(payment.getAmount()).max(BigDecimal.ZERO);
            invoice.updatePaymentStatus(newPaid);
            invoiceRepository.save(invoice);
        }

        paymentRepository.delete(payment);
    }

    @Override
    @Transactional
    public void deletePayments(List<Long> ids) {
        log.info("Batch deleting payments: {}", ids);
        for (Long id : ids) {
            deletePayment(id);
        }
    }

    private String generatePaymentNumber() {
        long count = paymentRepository.count() + 1;
        return String.format("REG-%d-%03d", LocalDate.now().getYear(), count);
    }
}
