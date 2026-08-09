package com.novaerp.dashboard.service;

import com.novaerp.client.repository.ClientRepository;
import com.novaerp.dashboard.dto.*;
import com.novaerp.invoice.entity.InvoiceStatus;
import com.novaerp.invoice.repository.InvoiceRepository;
import com.novaerp.payment.entity.Payment;
import com.novaerp.payment.repository.PaymentRepository;
import com.novaerp.product.dto.ProductDTO;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final ClientRepository clientRepository;
    private final SupplierRepository supplierRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        log.info("Calculating real-time dashboard KPIs for frontend and enterprise reporting");

        List<SalesOrder> allSales = salesOrderRepository.findAll();
        List<PurchaseOrder> allPurchases = purchaseOrderRepository.findAll();
        List<Stock> allStocks = stockRepository.findAll();
        List<Product> allProducts = productRepository.findAll();
        List<SalesOrderItem> allSaleItems = salesOrderItemRepository.findAll();

        // 1. Chiffre d'affaires & Profit
        BigDecimal totalVentes = allSales.stream()
                .filter(s -> s.getStatus() != SaleStatus.ANNULEE)
                .map(SalesOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCostOfGoodsSold = allSaleItems.stream()
                .filter(item -> item.getSalesOrder() != null && item.getSalesOrder().getStatus() != SaleStatus.ANNULEE)
                .map(item -> item.getProduct().getPurchasePrice().multiply(item.getQuantityOrdered()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal benefice = totalVentes.subtract(totalCostOfGoodsSold);

        BigDecimal totalAchats = allPurchases.stream()
                .filter(p -> p.getStatus() != PurchaseStatus.ANNULE)
                .map(PurchaseOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valeurStock = allStocks.stream()
                .map(s -> s.getQuantityOnHand().multiply(s.getProduct().getPurchasePrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalClients = clientRepository.count();
        long totalFournisseurs = supplierRepository.count();

        long facturesEnAttente = invoiceRepository.findAll().stream()
                .filter(i -> i.getStatus() == InvoiceStatus.VALIDEE || i.getStatus() == InvoiceStatus.PARTIELLEMENT_PAYEE || i.getStatus() == InvoiceStatus.EN_RETARD)
                .count();

        long alertesStock = allStocks.stream()
                .filter(s -> s.getQuantityAvailable().compareTo(s.getProduct().getMinStockLevel()) <= 0)
                .count();

        LocalDate now = LocalDate.now();
        LocalDate startCurrentMonth = now.withDayOfMonth(1);
        LocalDate startPreviousMonth = startCurrentMonth.minusMonths(1);
        LocalDate endPreviousMonth = startCurrentMonth.minusDays(1);

        BigDecimal caCurrentMonth = allSales.stream()
                .filter(s -> s.getStatus() != SaleStatus.ANNULEE && !s.getOrderDate().isBefore(startCurrentMonth))
                .map(SalesOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal caPreviousMonth = allSales.stream()
                .filter(s -> s.getStatus() != SaleStatus.ANNULEE && !s.getOrderDate().isBefore(startPreviousMonth) && !s.getOrderDate().isAfter(endPreviousMonth))
                .map(SalesOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double evolutionVentes = 0.0;
        if (caPreviousMonth.compareTo(BigDecimal.ZERO) > 0) {
            evolutionVentes = caCurrentMonth.subtract(caPreviousMonth)
                    .divide(caPreviousMonth, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        } else if (caCurrentMonth.compareTo(BigDecimal.ZERO) > 0) {
            evolutionVentes = 100.0;
        }

        BigDecimal achatsCurrentMonth = allPurchases.stream()
                .filter(p -> p.getStatus() != PurchaseStatus.ANNULE && !p.getOrderDate().isBefore(startCurrentMonth))
                .map(PurchaseOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal achatsPreviousMonth = allPurchases.stream()
                .filter(p -> p.getStatus() != PurchaseStatus.ANNULE && !p.getOrderDate().isBefore(startPreviousMonth) && !p.getOrderDate().isAfter(endPreviousMonth))
                .map(PurchaseOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double evolutionAchats = 0.0;
        if (achatsPreviousMonth.compareTo(BigDecimal.ZERO) > 0) {
            evolutionAchats = achatsCurrentMonth.subtract(achatsPreviousMonth)
                    .divide(achatsPreviousMonth, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        } else if (achatsCurrentMonth.compareTo(BigDecimal.ZERO) > 0) {
            evolutionAchats = 100.0;
        }

        // 2. Top Clients (aggregated from Sales Orders)
        Map<Long, BigDecimal> clientTotals = new HashMap<>();
        Map<Long, String> clientNames = new HashMap<>();
        for (SalesOrder s : allSales) {
            if (s.getStatus() != SaleStatus.ANNULEE && s.getClient() != null) {
                Long cId = s.getClient().getId();
                clientTotals.put(cId, clientTotals.getOrDefault(cId, BigDecimal.ZERO).add(s.getTotalAmount()));
                clientNames.put(cId, s.getClient().getName());
            }
        }
        List<TopClientDTO> topClients = clientTotals.entrySet().stream()
                .map(e -> TopClientDTO.builder().clientId(e.getKey()).nom(clientNames.get(e.getKey())).total(e.getValue()).build())
                .sorted((a, b) -> b.getTotal().compareTo(a.getTotal()))
                .limit(5)
                .collect(Collectors.toList());

        // 3. Top Products
        List<TopProductDTO> topProduits = getTopProducts(5);

        // 4. Low stock products
        List<ProductDTO> lowStockProducts = allProducts.stream()
                .map(p -> {
                    BigDecimal stockQty = stockRepository.getTotalQuantityOnHandByProductId(p.getId());
                    return ProductDTO.fromEntity(p, stockQty);
                })
                .filter(p -> p.getQuantiteStock().compareTo(p.getSeuilMinimum()) <= 0)
                .collect(Collectors.toList());

        // 5. Dormant products (no recent sales in last 30 days)
        Set<Long> activeProductIds = allSaleItems.stream()
                .filter(i -> i.getSalesOrder() != null && i.getSalesOrder().getOrderDate().isAfter(LocalDate.now().minusDays(30)))
                .map(i -> i.getProduct().getId())
                .collect(Collectors.toSet());

        List<ProductDTO> dormantProducts = allProducts.stream()
                .filter(p -> !activeProductIds.contains(p.getId()))
                .map(p -> {
                    BigDecimal stockQty = stockRepository.getTotalQuantityOnHandByProductId(p.getId());
                    return ProductDTO.fromEntity(p, stockQty);
                })
                .limit(5)
                .collect(Collectors.toList());

        return DashboardStatsDTO.builder()
                .chiffreAffaires(totalVentes)
                .benefice(benefice)
                .topClients(topClients)
                .topProduits(topProduits)
                .produitsStockFaible(lowStockProducts)
                .produitsDormants(dormantProducts)
                .totalVentes(totalVentes)
                .totalAchats(totalAchats)
                .valeurStock(valeurStock)
                .totalClients(totalClients)
                .totalFournisseurs(totalFournisseurs)
                .facturesEnAttente(facturesEnAttente)
                .alertesStock(alertesStock)
                .chiffreAffairesMois(caCurrentMonth)
                .evolutionVentes(evolutionVentes)
                .evolutionAchats(evolutionAchats)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesTrendDTO> getSalesTrends() {
        log.info("Aggregating sales and purchase monthly trends");

        List<SalesOrder> allSales = salesOrderRepository.findAll();
        List<PurchaseOrder> allPurchases = purchaseOrderRepository.findAll();

        List<SalesTrendDTO> trends = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            int year = monthDate.getYear();
            int month = monthDate.getMonthValue();
            String monthName = monthDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH);

            BigDecimal salesSum = allSales.stream()
                    .filter(s -> s.getStatus() != SaleStatus.ANNULEE && s.getOrderDate().getYear() == year && s.getOrderDate().getMonthValue() == month)
                    .map(SalesOrder::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal purchaseSum = allPurchases.stream()
                    .filter(p -> p.getStatus() != PurchaseStatus.ANNULE && p.getOrderDate().getYear() == year && p.getOrderDate().getMonthValue() == month)
                    .map(PurchaseOrder::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            trends.add(SalesTrendDTO.builder()
                    .mois(monthName.substring(0, 1).toUpperCase() + monthName.substring(1))
                    .ventes(salesSum)
                    .achats(purchaseSum)
                    .build());
        }

        return trends;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopProductDTO> getTopProducts(int limit) {
        log.info("Calculating top {} selling products", limit);

        List<SalesOrderItem> allItems = salesOrderItemRepository.findAll();
        Map<Product, List<SalesOrderItem>> byProduct = allItems.stream()
                .collect(Collectors.groupingBy(SalesOrderItem::getProduct));

        return byProduct.entrySet().stream()
                .map(entry -> {
                    Product prod = entry.getKey();
                    BigDecimal totalQty = entry.getValue().stream()
                            .map(SalesOrderItem::getQuantityOrdered)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalRevenue = entry.getValue().stream()
                            .map(SalesOrderItem::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return TopProductDTO.builder()
                            .id(prod.getId())
                            .nom(prod.getName())
                            .sku(prod.getSku())
                            .quantiteVendue(totalQty)
                            .chiffreAffaires(totalRevenue)
                            .build();
                })
                .sorted((a, b) -> b.getChiffreAffaires().compareTo(a.getChiffreAffaires()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecentActivityDTO> getRecentActivities(int limit) {
        log.info("Fetching {} recent activities", limit);

        List<RecentActivityDTO> activities = new ArrayList<>();

        // 1. Sales
        salesOrderRepository.findAll(PageRequest.of(0, 5, Sort.by("createdAt").descending())).forEach(s -> {
            activities.add(RecentActivityDTO.builder()
                    .id(s.getId())
                    .type("VENTE")
                    .description("Commande client " + s.getOrderNumber() + " (" + (s.getClient() != null ? s.getClient().getName() : "Client") + ")")
                    .date(s.getCreatedAt())
                    .montant(s.getTotalAmount())
                    .statut(s.getStatus().name())
                    .build());
        });

        // 2. Purchases
        purchaseOrderRepository.findAll(PageRequest.of(0, 5, Sort.by("createdAt").descending())).forEach(p -> {
            activities.add(RecentActivityDTO.builder()
                    .id(p.getId())
                    .type("ACHAT")
                    .description("Commande fournisseur " + p.getOrderNumber() + " (" + (p.getSupplier() != null ? p.getSupplier().getName() : "Fournisseur") + ")")
                    .date(p.getCreatedAt())
                    .montant(p.getTotalAmount())
                    .statut(p.getStatus().name())
                    .build());
        });

        // 3. Payments
        paymentRepository.findAll(PageRequest.of(0, 5, Sort.by("createdAt").descending())).forEach(pay -> {
            activities.add(RecentActivityDTO.builder()
                    .id(pay.getId())
                    .type("PAIEMENT")
                    .description("Paiement " + pay.getPaymentNumber() + (pay.getClient() != null ? " (" + pay.getClient().getName() + ")" : ""))
                    .date(pay.getCreatedAt())
                    .montant(pay.getAmount())
                    .statut(pay.getStatus().name())
                    .build());
        });

        return activities.stream()
                .sorted((a, b) -> {
                    if (a.getDate() == null && b.getDate() == null) return 0;
                    if (a.getDate() == null) return 1;
                    if (b.getDate() == null) return -1;
                    return b.getDate().compareTo(a.getDate());
                })
                .limit(limit)
                .collect(Collectors.toList());
    }
}
