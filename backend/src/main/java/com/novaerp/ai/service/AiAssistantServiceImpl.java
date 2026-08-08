package com.novaerp.ai.service;

import com.novaerp.ai.dto.AiAnomalyDTO;
import com.novaerp.ai.dto.AiChatRequestDTO;
import com.novaerp.ai.dto.AiMessageDTO;
import com.novaerp.ai.dto.AiPredictionDTO;
import com.novaerp.ai.entity.AiAnomaly;
import com.novaerp.ai.entity.AnomalyStatus;
import com.novaerp.ai.repository.AiAnomalyRepository;
import com.novaerp.exception.ResourceNotFoundException;
import com.novaerp.invoice.entity.Invoice;
import com.novaerp.invoice.entity.InvoiceStatus;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private final AiAnomalyRepository aiAnomalyRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final InvoiceRepository invoiceRepository;
    private final SupplierRepository supplierRepository;

    @Override
    @Transactional(readOnly = true)
    public AiMessageDTO chat(AiChatRequestDTO request) {
        String query = request.getContent() != null ? request.getContent().trim().toLowerCase() : "";
        log.info("Processing AI Assistant chat query: {}", query);

        String responseContent;
        String intent = "GENERAL_INQUIRY";
        Map<String, Object> entities = new HashMap<>();
        List<AiMessageDTO.ActionSuggestion> actions = new ArrayList<>();

        if (query.contains("vente") || query.contains("chiffre d'affaires") || query.contains("ca") || query.contains("revenu")) {
            intent = "SALES_QUERY";
            List<SalesOrder> sales = salesOrderRepository.findAll();
            BigDecimal totalCa = sales.stream()
                    .filter(s -> s.getStatus() != SaleStatus.ANNULEE)
                    .map(SalesOrder::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long totalOrders = sales.stream().filter(s -> s.getStatus() != SaleStatus.ANNULEE).count();

            responseContent = String.format(
                    "📊 **Analyse des Ventes :** Le chiffre d'affaires cumulé actuel s'élève à **%.2f MAD** réparti sur **%d commandes validées**. La dynamique commerciale est positive avec un panier moyen stable.",
                    totalCa, totalOrders
            );
            entities.put("totalVentes", totalCa);
            entities.put("nombreCommandes", totalOrders);
            actions.add(new AiMessageDTO.ActionSuggestion("Voir les commandes de vente", "NAVIGATE", "/sales"));
        } else if (query.contains("stock") || query.contains("rupture") || query.contains("inventaire") || query.contains("produit")) {
            intent = "STOCK_QUERY";
            List<Stock> stocks = stockRepository.findAll();
            List<Stock> lowStocks = stocks.stream()
                    .filter(s -> s.getQuantityAvailable().compareTo(s.getProduct().getMinStockLevel()) <= 0)
                    .collect(Collectors.toList());

            if (lowStocks.isEmpty()) {
                responseContent = "✅ **Santé des Stocks :** Tous les articles sont actuellement à des niveaux nominaux au-dessus des seuils de sécurité.";
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("⚠️ **Alerte Réapprovisionnement :** ").append(lowStocks.size()).append(" article(s) sont sous le seuil critique :\n");
                for (Stock ls : lowStocks) {
                    sb.append(String.format("- **%s** (%s) : %.0f en stock (Seuil min : %.0f)\n",
                            ls.getProduct().getName(), ls.getProduct().getSku(),
                            ls.getQuantityAvailable(), ls.getProduct().getMinStockLevel()));
                }
                sb.append("\nIl est conseillé de générer des bons de commande fournisseurs sans tarder.");
                responseContent = sb.toString();
            }
            entities.put("alertesStockCount", lowStocks.size());
            actions.add(new AiMessageDTO.ActionSuggestion("Consulter les prédictions IA", "NAVIGATE", "/ai-assistant"));
            actions.add(new AiMessageDTO.ActionSuggestion("Gérer le stock", "NAVIGATE", "/stock"));
        } else if (query.contains("facture") || query.contains("impay") || query.contains("paiement") || query.contains("règlement") || query.contains("reglement")) {
            intent = "FINANCE_QUERY";
            List<Invoice> invoices = invoiceRepository.findAll();
            List<Invoice> unpaid = invoices.stream()
                    .filter(i -> i.getStatus() == InvoiceStatus.VALIDEE || i.getStatus() == InvoiceStatus.PARTIELLEMENT_PAYEE || i.getStatus() == InvoiceStatus.EN_RETARD)
                    .collect(Collectors.toList());
            BigDecimal totalDue = unpaid.stream()
                    .map(i -> i.getTotalAmount().subtract(i.getPaidAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            responseContent = String.format(
                    "💳 **Situation de Facturation :** Vous avez **%d facture(s) en attente de règlement** pour un montant restant dû de **%.2f MAD**. Pensez à programmer des relances clients.",
                    unpaid.size(), totalDue
            );
            entities.put("facturesEnAttente", unpaid.size());
            entities.put("montantRestantDu", totalDue);
            actions.add(new AiMessageDTO.ActionSuggestion("Voir les factures impayées", "NAVIGATE", "/invoices"));
        } else {
            responseContent = "Bonjour ! Je suis **NovaAI**, votre copilote décisionnel d'entreprise. 🚀\n\nJe peux vous assister en temps réel sur :\n- 📈 L'analyse financière et l'évolution du chiffre d'affaires\n- 📦 La prédiction des ruptures de stocks et suggestions de commandes\n- 🔍 La détection d'anomalies de prix et de retards fournisseurs\n- 📄 Le suivi des créances et factures impayées\n\nQue souhaitez-vous analyser aujourd'hui ?";
            actions.add(new AiMessageDTO.ActionSuggestion("Vérifier le stock critique", "QUERY", "Quels sont les produits en rupture de stock ?"));
            actions.add(new AiMessageDTO.ActionSuggestion("Consulter le CA", "QUERY", "Quel est le chiffre d'affaires global ?"));
            actions.add(new AiMessageDTO.ActionSuggestion("Factures en retard", "QUERY", "Quelles sont les factures impayées ?"));
        }

        AiMessageDTO.MessageMetadata metadata = AiMessageDTO.MessageMetadata.builder()
                .intent(intent)
                .confidence(0.98)
                .entities(entities)
                .actions(actions)
                .build();

        return AiMessageDTO.builder()
                .role("assistant")
                .content(responseContent)
                .timestamp(LocalDateTime.now())
                .metadata(metadata)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiPredictionDTO> getStockPredictions() {
        log.info("Generating AI stock predictions and forecasting runout dates");

        List<Stock> stocks = stockRepository.findAll();
        List<Supplier> suppliers = supplierRepository.findAll();
        List<AiPredictionDTO> predictions = new ArrayList<>();

        for (Stock stock : stocks) {
            Product prod = stock.getProduct();
            BigDecimal available = stock.getQuantityAvailable();
            BigDecimal minStock = prod.getMinStockLevel() != null ? prod.getMinStockLevel() : BigDecimal.valueOf(10);

            // Calculate heuristic average daily consumption
            BigDecimal avgDailyConsumption = BigDecimal.valueOf(5.0);
            if (prod.getCategory() != null && prod.getCategory().contains("Alimentaire")) {
                avgDailyConsumption = BigDecimal.valueOf(8.5);
            }

            int joursRestants = 0;
            if (avgDailyConsumption.compareTo(BigDecimal.ZERO) > 0) {
                joursRestants = available.divide(avgDailyConsumption, 0, RoundingMode.DOWN).intValue();
            }

            LocalDate projectedDate = LocalDate.now().plusDays(joursRestants);

            String recommandation;
            BigDecimal recommendedQty;

            if (joursRestants < 7) {
                recommandation = "COMMANDER_URGENT";
                recommendedQty = minStock.multiply(BigDecimal.valueOf(3)).subtract(available).max(BigDecimal.valueOf(50));
            } else if (joursRestants < 20) {
                recommandation = "COMMANDER_BIENTOT";
                recommendedQty = minStock.multiply(BigDecimal.valueOf(2)).subtract(available).max(BigDecimal.valueOf(20));
            } else if (joursRestants > 60) {
                recommandation = "SURSTOCK";
                recommendedQty = BigDecimal.ZERO;
            } else {
                recommandation = "STOCK_OPTIMAL";
                recommendedQty = BigDecimal.ZERO;
            }

            AiPredictionDTO.SuggestedSupplierDTO suggestedSupplier = null;
            if (!suppliers.isEmpty()) {
                Supplier s = suppliers.get(0);
                suggestedSupplier = AiPredictionDTO.SuggestedSupplierDTO.builder()
                        .id(s.getId())
                        .nom(s.getName())
                        .prixUnitaire(prod.getPurchasePrice())
                        .delaiLivraisonJours(3)
                        .build();
            }

            predictions.add(AiPredictionDTO.builder()
                    .produitId(prod.getId())
                    .produitNom(prod.getName())
                    .sku(prod.getSku())
                    .stockActuel(available)
                    .consommationMoyenne(avgDailyConsumption)
                    .joursRestants(joursRestants)
                    .dateRupturePrevue(projectedDate)
                    .recommandation(recommandation)
                    .quantiteRecommandee(recommendedQty)
                    .fournisseurSuggere(suggestedSupplier)
                    .build());
        }

        return predictions;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiAnomalyDTO> getAnomalies() {
        log.info("Fetching detected AI anomalies");
        return aiAnomalyRepository.findAllByOrderByDetectionDateDesc().stream()
                .map(AiAnomalyDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void resolveAnomaly(Long id, AnomalyStatus status) {
        log.info("Resolving anomaly ID {} with status: {}", id, status);
        AiAnomaly anomaly = aiAnomalyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Anomaly not found with id: " + id));
        anomaly.setStatus(status != null ? status : AnomalyStatus.RESOLU);
        aiAnomalyRepository.save(anomaly);
    }
}
