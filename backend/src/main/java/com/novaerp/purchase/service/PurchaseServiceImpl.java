package com.novaerp.purchase.service;

import com.novaerp.exception.BusinessRuleException;
import com.novaerp.exception.ResourceNotFoundException;
import com.novaerp.product.entity.Product;
import com.novaerp.product.repository.ProductRepository;
import com.novaerp.purchase.dto.PurchaseDTO;
import com.novaerp.purchase.dto.PurchaseItemDTO;
import com.novaerp.purchase.entity.PurchaseOrder;
import com.novaerp.purchase.entity.PurchaseOrderItem;
import com.novaerp.purchase.entity.PurchaseStatus;
import com.novaerp.purchase.repository.PurchaseOrderRepository;
import com.novaerp.stock.entity.Stock;
import com.novaerp.stock.entity.StockMovement;
import com.novaerp.stock.entity.StockMovementType;
import com.novaerp.stock.entity.Warehouse;
import com.novaerp.stock.repository.StockMovementRepository;
import com.novaerp.stock.repository.StockRepository;
import com.novaerp.stock.repository.WarehouseRepository;
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
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseDTO> getPurchases(Pageable pageable, String search) {
        Page<PurchaseOrder> page = StringUtils.hasText(search)
                ? purchaseOrderRepository.searchPurchases(search, pageable)
                : purchaseOrderRepository.findAll(pageable);
        return page.map(PurchaseDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseDTO getPurchaseById(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
        return PurchaseDTO.fromEntity(order);
    }

    @Override
    @Transactional
    public PurchaseDTO createPurchase(PurchaseDTO dto) {
        log.info("Creating purchase order for supplier ID: {}", dto.getFournisseurId());

        Supplier supplier = supplierRepository.findById(dto.getFournisseurId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + dto.getFournisseurId()));

        Warehouse warehouse = dto.getWarehouseId() != null
                ? warehouseRepository.findById(dto.getWarehouseId()).orElse(null)
                : warehouseRepository.findAll().stream().findFirst().orElse(null);

        String reference = StringUtils.hasText(dto.getReference())
                ? dto.getReference().trim()
                : generatePurchaseOrderNumber();

        PurchaseOrder order = PurchaseOrder.builder()
                .orderNumber(reference)
                .supplier(supplier)
                .warehouse(warehouse)
                .status(dto.getStatut() != null ? dto.getStatut() : PurchaseStatus.EN_ATTENTE)
                .orderDate(dto.getDate() != null ? dto.getDate() : LocalDate.now())
                .taxRate(dto.getTva() != null ? dto.getTva() : BigDecimal.valueOf(20.00))
                .notes(dto.getNotes())
                .build();

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (PurchaseItemDTO itemDto : dto.getItems()) {
                Product product = productRepository.findById(itemDto.getProduitId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDto.getProduitId()));

                PurchaseOrderItem item = PurchaseOrderItem.builder()
                        .product(product)
                        .quantityOrdered(itemDto.getQuantite())
                        .quantityReceived(BigDecimal.ZERO)
                        .unitPrice(itemDto.getPrixUnitaire())
                        .taxRate(order.getTaxRate())
                        .build();
                item.recalculateTotals();
                order.addItem(item);
            }
        }

        order.recalculateTotals();
        PurchaseOrder saved = purchaseOrderRepository.save(order);

        // If status was initially set to RECUE, immediately process receipt
        if (saved.getStatus() == PurchaseStatus.RECUE) {
            processStockReceipt(saved);
        }

        return PurchaseDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public List<PurchaseDTO> createPurchases(List<PurchaseDTO> dtos) {
        log.info("Batch creating {} purchase orders", dtos.size());
        List<PurchaseDTO> results = new ArrayList<>();
        for (PurchaseDTO dto : dtos) {
            results.add(createPurchase(dto));
        }
        return results;
    }

    @Override
    @Transactional
    public PurchaseDTO updatePurchase(Long id, PurchaseDTO dto) {
        log.info("Updating purchase order ID: {}", id);

        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));

        if (order.getStatus() == PurchaseStatus.RECUE) {
            throw new BusinessRuleException("Cannot modify an already received purchase order");
        }

        if (dto.getFournisseurId() != null) {
            Supplier supplier = supplierRepository.findById(dto.getFournisseurId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + dto.getFournisseurId()));
            order.setSupplier(supplier);
        }

        if (dto.getStatut() != null) {
            PurchaseStatus oldStatus = order.getStatus();
            order.setStatus(dto.getStatut());
            if (dto.getStatut() == PurchaseStatus.RECUE && oldStatus != PurchaseStatus.RECUE) {
                processStockReceipt(order);
            }
        }

        if (dto.getDate() != null) {
            order.setOrderDate(dto.getDate());
        }
        if (dto.getNotes() != null) {
            order.setNotes(dto.getNotes());
        }

        if (dto.getItems() != null && !dto.getItems().isEmpty() && order.getStatus() != PurchaseStatus.RECUE) {
            order.getItems().clear();
            for (PurchaseItemDTO itemDto : dto.getItems()) {
                Product product = productRepository.findById(itemDto.getProduitId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDto.getProduitId()));

                PurchaseOrderItem item = PurchaseOrderItem.builder()
                        .product(product)
                        .quantityOrdered(itemDto.getQuantite())
                        .quantityReceived(BigDecimal.ZERO)
                        .unitPrice(itemDto.getPrixUnitaire())
                        .taxRate(order.getTaxRate())
                        .build();
                item.recalculateTotals();
                order.addItem(item);
            }
            order.recalculateTotals();
        }

        PurchaseOrder saved = purchaseOrderRepository.save(order);
        return PurchaseDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public PurchaseDTO receivePurchase(Long id) {
        log.info("Receiving goods for purchase order ID: {}", id);

        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));

        if (order.getStatus() == PurchaseStatus.RECUE) {
            throw new BusinessRuleException("Purchase order is already received");
        }
        if (order.getStatus() == PurchaseStatus.ANNULE) {
            throw new BusinessRuleException("Cannot receive a cancelled purchase order");
        }

        order.setStatus(PurchaseStatus.RECUE);
        processStockReceipt(order);

        PurchaseOrder saved = purchaseOrderRepository.save(order);
        return PurchaseDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deletePurchase(Long id) {
        log.info("Deleting purchase order ID: {}", id);
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
        purchaseOrderRepository.delete(order);
    }

    @Override
    @Transactional
    public void deletePurchases(List<Long> ids) {
        log.info("Batch deleting purchase orders: {}", ids);
        purchaseOrderRepository.deleteAllById(ids);
    }

    private void processStockReceipt(PurchaseOrder order) {
        Warehouse warehouse = order.getWarehouse() != null
                ? order.getWarehouse()
                : warehouseRepository.findAll().stream().findFirst().orElse(null);

        for (PurchaseOrderItem item : order.getItems()) {
            item.setQuantityReceived(item.getQuantityOrdered());

            if (warehouse != null) {
                Stock stock = stockRepository.findByProductIdAndWarehouseId(item.getProduct().getId(), warehouse.getId())
                        .orElseGet(() -> Stock.builder()
                                .product(item.getProduct())
                                .warehouse(warehouse)
                                .quantityOnHand(BigDecimal.ZERO)
                                .quantityAllocated(BigDecimal.ZERO)
                                .quantityAvailable(BigDecimal.ZERO)
                                .build());

                stock.setQuantityOnHand(stock.getQuantityOnHand().add(item.getQuantityOrdered()));
                stock.recalculateAvailable();
                stockRepository.save(stock);

                StockMovement movement = StockMovement.builder()
                        .product(item.getProduct())
                        .targetWarehouse(warehouse)
                        .movementType(StockMovementType.IN_PURCHASE)
                        .quantity(item.getQuantityOrdered())
                        .unitCost(item.getUnitPrice())
                        .referenceType("PURCHASE_ORDER")
                        .referenceId(order.getOrderNumber())
                        .notes("Reception bon de commande " + order.getOrderNumber())
                        .build();

                stockMovementRepository.save(movement);
            }
        }
    }

    private String generatePurchaseOrderNumber() {
        long count = purchaseOrderRepository.count() + 1;
        return String.format("ACH-%d-%03d", LocalDate.now().getYear(), count);
    }
}
