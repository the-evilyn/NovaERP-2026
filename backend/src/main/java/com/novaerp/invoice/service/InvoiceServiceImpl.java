package com.novaerp.invoice.service;

import com.novaerp.client.entity.Client;
import com.novaerp.client.repository.ClientRepository;
import com.novaerp.exception.BusinessRuleException;
import com.novaerp.exception.ResourceNotFoundException;
import com.novaerp.invoice.dto.InvoiceDTO;
import com.novaerp.invoice.dto.InvoiceItemDTO;
import com.novaerp.invoice.entity.Invoice;
import com.novaerp.invoice.entity.InvoiceItem;
import com.novaerp.invoice.entity.InvoiceStatus;
import com.novaerp.invoice.entity.InvoiceType;
import com.novaerp.invoice.repository.InvoiceRepository;
import com.novaerp.product.entity.Product;
import com.novaerp.product.repository.ProductRepository;
import com.novaerp.sale.entity.SaleStatus;
import com.novaerp.sale.entity.SalesOrder;
import com.novaerp.sale.entity.SalesOrderItem;
import com.novaerp.sale.repository.SalesOrderRepository;
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
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final SalesOrderRepository salesOrderRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceDTO> getInvoices(Pageable pageable, String search) {
        Page<Invoice> page = StringUtils.hasText(search)
                ? invoiceRepository.searchInvoices(search, pageable)
                : invoiceRepository.findAll(pageable);
        return page.map(InvoiceDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDTO getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
        return InvoiceDTO.fromEntity(invoice);
    }

    @Override
    @Transactional
    public InvoiceDTO createInvoice(InvoiceDTO dto) {
        log.info("Creating invoice for client ID: {}", dto.getClientId());

        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + dto.getClientId()));

        SalesOrder salesOrder = dto.getSalesOrderId() != null
                ? salesOrderRepository.findById(dto.getSalesOrderId()).orElse(null)
                : null;

        String reference = StringUtils.hasText(dto.getReference())
                ? dto.getReference().trim()
                : generateInvoiceNumber();

        LocalDate issueDate = dto.getDate() != null ? dto.getDate() : LocalDate.now();
        LocalDate dueDate = dto.getDateEcheance() != null
                ? dto.getDateEcheance()
                : issueDate.plusDays(client.getPaymentTerms() != null ? client.getPaymentTerms() : 30);

        Invoice invoice = Invoice.builder()
                .invoiceNumber(reference)
                .client(client)
                .salesOrder(salesOrder)
                .type(dto.getType() != null ? dto.getType() : InvoiceType.STANDARD)
                .status(dto.getStatut() != null ? dto.getStatut() : InvoiceStatus.VALIDEE)
                .issueDate(issueDate)
                .dueDate(dueDate)
                .taxRate(dto.getTva() != null ? dto.getTva() : BigDecimal.valueOf(20.00))
                .discountAmount(dto.getDiscountAmount() != null ? dto.getDiscountAmount() : BigDecimal.ZERO)
                .paidAmount(dto.getMontantPaye() != null ? dto.getMontantPaye() : BigDecimal.ZERO)
                .notes(dto.getNotes())
                .build();

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (InvoiceItemDTO itemDto : dto.getItems()) {
                Product product = productRepository.findById(itemDto.getProduitId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDto.getProduitId()));

                InvoiceItem item = InvoiceItem.builder()
                        .product(product)
                        .quantity(itemDto.getQuantite())
                        .unitPrice(itemDto.getPrixUnitaire())
                        .taxRate(invoice.getTaxRate())
                        .build();
                item.recalculateTotals();
                invoice.addItem(item);
            }
        }

        invoice.recalculateTotals();
        invoice.updatePaymentStatus(invoice.getPaidAmount());
        Invoice saved = invoiceRepository.save(invoice);

        if (salesOrder != null) {
            salesOrder.setStatus(SaleStatus.FACTUREE);
            salesOrderRepository.save(salesOrder);
        }

        return InvoiceDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public List<InvoiceDTO> createInvoices(List<InvoiceDTO> dtos) {
        log.info("Batch creating {} invoices", dtos.size());
        List<InvoiceDTO> results = new ArrayList<>();
        for (InvoiceDTO dto : dtos) {
            results.add(createInvoice(dto));
        }
        return results;
    }

    @Override
    @Transactional
    public InvoiceDTO createInvoiceFromSale(Long saleId) {
        log.info("Generating invoice from sales order ID: {}", saleId);

        SalesOrder salesOrder = salesOrderRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales order not found with id: " + saleId));

        if (salesOrder.getStatus() == SaleStatus.FACTUREE) {
            throw new BusinessRuleException("Sales order is already invoiced");
        }

        InvoiceDTO dto = InvoiceDTO.builder()
                .clientId(salesOrder.getClient().getId())
                .salesOrderId(salesOrder.getId())
                .type(InvoiceType.STANDARD)
                .statut(InvoiceStatus.VALIDEE)
                .date(LocalDate.now())
                .dateEcheance(LocalDate.now().plusDays(salesOrder.getClient().getPaymentTerms() != null ? salesOrder.getClient().getPaymentTerms() : 30))
                .tva(salesOrder.getTaxRate())
                .discountAmount(salesOrder.getDiscountAmount())
                .notes("Facture issue de la commande " + salesOrder.getOrderNumber())
                .items(new ArrayList<>())
                .build();

        for (SalesOrderItem soItem : salesOrder.getItems()) {
            dto.getItems().add(InvoiceItemDTO.builder()
                    .produitId(soItem.getProduct().getId())
                    .produitNom(soItem.getProduct().getName())
                    .quantite(soItem.getQuantityOrdered())
                    .prixUnitaire(soItem.getUnitPrice())
                    .build());
        }

        return createInvoice(dto);
    }

    @Override
    @Transactional
    public InvoiceDTO updateInvoice(Long id, InvoiceDTO dto) {
        log.info("Updating invoice ID: {}", id);

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        if (invoice.getStatus() == InvoiceStatus.PAYEE) {
            throw new BusinessRuleException("Cannot modify an already paid invoice");
        }

        if (dto.getClientId() != null) {
            Client client = clientRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + dto.getClientId()));
            invoice.setClient(client);
        }

        if (dto.getDate() != null) {
            invoice.setIssueDate(dto.getDate());
        }
        if (dto.getDateEcheance() != null) {
            invoice.setDueDate(dto.getDateEcheance());
        }
        if (dto.getNotes() != null) {
            invoice.setNotes(dto.getNotes());
        }
        if (dto.getMontantPaye() != null) {
            invoice.updatePaymentStatus(dto.getMontantPaye());
        }
        if (dto.getStatut() != null) {
            invoice.setStatus(dto.getStatut());
        }

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            invoice.getItems().clear();
            for (InvoiceItemDTO itemDto : dto.getItems()) {
                Product product = productRepository.findById(itemDto.getProduitId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDto.getProduitId()));

                InvoiceItem item = InvoiceItem.builder()
                        .product(product)
                        .quantity(itemDto.getQuantite())
                        .unitPrice(itemDto.getPrixUnitaire())
                        .taxRate(invoice.getTaxRate())
                        .build();
                item.recalculateTotals();
                invoice.addItem(item);
            }
            invoice.recalculateTotals();
            invoice.updatePaymentStatus(invoice.getPaidAmount());
        }

        Invoice saved = invoiceRepository.save(invoice);
        return InvoiceDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deleteInvoice(Long id) {
        log.info("Deleting invoice ID: {}", id);
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
        invoiceRepository.delete(invoice);
    }

    @Override
    @Transactional
    public void deleteInvoices(List<Long> ids) {
        log.info("Batch deleting invoices: {}", ids);
        invoiceRepository.deleteAllById(ids);
    }

    private String generateInvoiceNumber() {
        long count = invoiceRepository.count() + 1;
        return String.format("FAC-%d-%03d", LocalDate.now().getYear(), count);
    }
}
