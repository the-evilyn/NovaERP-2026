package com.novaerp.sale.service;

import com.novaerp.client.entity.Client;
import com.novaerp.client.repository.ClientRepository;
import com.novaerp.exception.BusinessRuleException;
import com.novaerp.exception.ResourceNotFoundException;
import com.novaerp.product.entity.Product;
import com.novaerp.product.repository.ProductRepository;
import com.novaerp.sale.dto.SaleDTO;
import com.novaerp.sale.dto.SaleItemDTO;
import com.novaerp.sale.entity.SaleStatus;
import com.novaerp.sale.entity.SalesOrder;
import com.novaerp.sale.entity.SalesOrderItem;
import com.novaerp.sale.repository.SalesOrderRepository;
import com.novaerp.stock.entity.Stock;
import com.novaerp.stock.entity.StockMovement;
import com.novaerp.stock.entity.StockMovementType;
import com.novaerp.stock.entity.Warehouse;
import com.novaerp.stock.repository.StockMovementRepository;
import com.novaerp.stock.repository.StockRepository;
import com.novaerp.stock.repository.WarehouseRepository;
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
public class SaleServiceImpl implements SaleService {

    private final SalesOrderRepository salesOrderRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<SaleDTO> getSales(Pageable pageable, String search) {
        Page<SalesOrder> page = StringUtils.hasText(search)
                ? salesOrderRepository.searchSales(search, pageable)
                : salesOrderRepository.findAll(pageable);
        return page.map(SaleDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public SaleDTO getSaleById(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales order not found with id: " + id));
        return SaleDTO.fromEntity(order);
    }

    @Override
    @Transactional
    public SaleDTO createSale(SaleDTO dto) {
        log.info("Creating sales order for client ID: {}", dto.getClientId());

        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + dto.getClientId()));

        Warehouse warehouse = dto.getWarehouseId() != null
                ? warehouseRepository.findById(dto.getWarehouseId()).orElse(null)
                : warehouseRepository.findAll().stream().findFirst().orElse(null);

        String reference = StringUtils.hasText(dto.getReference())
                ? dto.getReference().trim()
                : generateSalesOrderNumber();

        SalesOrder order = SalesOrder.builder()
                .orderNumber(reference)
                .client(client)
                .warehouse(warehouse)
                .status(dto.getStatut() != null ? dto.getStatut() : SaleStatus.COMMANDE)
                .orderDate(dto.getDate() != null ? dto.getDate() : LocalDate.now())
                .taxRate(dto.getTva() != null ? dto.getTva() : BigDecimal.valueOf(20.00))
                .discountAmount(dto.getDiscountAmount() != null ? dto.getDiscountAmount() : BigDecimal.ZERO)
                .shippingCost(dto.getShippingCost() != null ? dto.getShippingCost() : BigDecimal.ZERO)
                .notes(dto.getNotes())
                .build();

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (SaleItemDTO itemDto : dto.getItems()) {
                Product product = productRepository.findById(itemDto.getProduitId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDto.getProduitId()));

                SalesOrderItem item = SalesOrderItem.builder()
                        .product(product)
                        .quantityOrdered(itemDto.getQuantite())
                        .quantityShipped(BigDecimal.ZERO)
                        .unitPrice(itemDto.getPrixUnitaire())
                        .taxRate(order.getTaxRate())
                        .build();
                item.recalculateTotals();
                order.addItem(item);
            }
        }

        order.recalculateTotals();
        SalesOrder saved = salesOrderRepository.save(order);

        // If status was set directly to LIVREE, execute delivery deduction
        if (saved.getStatus() == SaleStatus.LIVREE) {
            processDelivery(saved);
        }

        return SaleDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public List<SaleDTO> createSales(List<SaleDTO> dtos) {
        log.info("Batch creating {} sales orders", dtos.size());
        List<SaleDTO> results = new ArrayList<>();
        for (SaleDTO dto : dtos) {
            results.add(createSale(dto));
        }
        return results;
    }

    @Override
    @Transactional
    public SaleDTO updateSale(Long id, SaleDTO dto) {
        log.info("Updating sales order ID: {}", id);

        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales order not found with id: " + id));

        if (order.getStatus() == SaleStatus.LIVREE || order.getStatus() == SaleStatus.FACTUREE) {
            throw new BusinessRuleException("Cannot modify an already delivered or invoiced sales order");
        }

        if (dto.getClientId() != null) {
            Client client = clientRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + dto.getClientId()));
            order.setClient(client);
        }

        if (dto.getStatut() != null) {
            SaleStatus oldStatus = order.getStatus();
            order.setStatus(dto.getStatut());
            if (dto.getStatut() == SaleStatus.LIVREE && oldStatus != SaleStatus.LIVREE) {
                processDelivery(order);
            }
        }

        if (dto.getDate() != null) {
            order.setOrderDate(dto.getDate());
        }
        if (dto.getNotes() != null) {
            order.setNotes(dto.getNotes());
        }

        if (dto.getItems() != null && !dto.getItems().isEmpty() && order.getStatus() != SaleStatus.LIVREE) {
            order.getItems().clear();
            for (SaleItemDTO itemDto : dto.getItems()) {
                Product product = productRepository.findById(itemDto.getProduitId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDto.getProduitId()));

                SalesOrderItem item = SalesOrderItem.builder()
                        .product(product)
                        .quantityOrdered(itemDto.getQuantite())
                        .quantityShipped(BigDecimal.ZERO)
                        .unitPrice(itemDto.getPrixUnitaire())
                        .taxRate(order.getTaxRate())
                        .build();
                item.recalculateTotals();
                order.addItem(item);
            }
            order.recalculateTotals();
        }

        SalesOrder saved = salesOrderRepository.save(order);
        return SaleDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public SaleDTO deliverSale(Long id) {
        log.info("Processing delivery for sales order ID: {}", id);

        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales order not found with id: " + id));

        if (order.getStatus() == SaleStatus.LIVREE) {
            throw new BusinessRuleException("Sales order is already delivered");
        }
        if (order.getStatus() == SaleStatus.ANNULEE) {
            throw new BusinessRuleException("Cannot deliver a cancelled sales order");
        }

        order.setStatus(SaleStatus.LIVREE);
        processDelivery(order);

        SalesOrder saved = salesOrderRepository.save(order);
        return SaleDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deleteSale(Long id) {
        log.info("Deleting sales order ID: {}", id);
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales order not found with id: " + id));
        salesOrderRepository.delete(order);
    }

    @Override
    @Transactional
    public void deleteSales(List<Long> ids) {
        log.info("Batch deleting sales orders: {}", ids);
        salesOrderRepository.deleteAllById(ids);
    }

    private void processDelivery(SalesOrder order) {
        Warehouse warehouse = order.getWarehouse() != null
                ? order.getWarehouse()
                : warehouseRepository.findAll().stream().findFirst().orElse(null);

        for (SalesOrderItem item : order.getItems()) {
            item.setQuantityShipped(item.getQuantityOrdered());

            if (warehouse != null) {
                Stock stock = stockRepository.findByProductIdAndWarehouseId(item.getProduct().getId(), warehouse.getId())
                        .orElseThrow(() -> new BusinessRuleException("No inventory found for " + item.getProduct().getName() + " in warehouse"));

                if (stock.getQuantityAvailable().compareTo(item.getQuantityOrdered()) < 0) {
                    throw new BusinessRuleException("Insufficient stock to deliver " + item.getProduct().getName() +
                            ". Available: " + stock.getQuantityAvailable() + ", Requested: " + item.getQuantityOrdered());
                }

                stock.setQuantityOnHand(stock.getQuantityOnHand().subtract(item.getQuantityOrdered()));
                stock.recalculateAvailable();
                stockRepository.save(stock);

                StockMovement movement = StockMovement.builder()
                        .product(item.getProduct())
                        .sourceWarehouse(warehouse)
                        .movementType(StockMovementType.OUT_SALE)
                        .quantity(item.getQuantityOrdered())
                        .unitCost(item.getProduct().getPurchasePrice())
                        .referenceType("SALES_ORDER")
                        .referenceId(order.getOrderNumber())
                        .notes("Livraison commande client " + order.getOrderNumber())
                        .build();

                stockMovementRepository.save(movement);
            }
        }
    }

    private String generateSalesOrderNumber() {
        long count = salesOrderRepository.count() + 1;
        return String.format("VTE-%d-%03d", LocalDate.now().getYear(), count);
    }
}
