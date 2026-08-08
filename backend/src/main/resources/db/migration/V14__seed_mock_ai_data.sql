-- =============================================================================
-- NovaERP V14: Seed Mock AI Assistant Data & Anomalies
-- =============================================================================

INSERT INTO ai_anomalies (type, severity, title, description, entity_type, entity_id, status, detection_date, created_at, updated_at, created_by, version)
VALUES
('PRIX_ANORMAL', 'ELEVEE', 'Écart de prix d''achat détecté', 'Le prix unitaire sur la commande ACH-2026-002 pour Farine T55 (8.50 MAD) est supérieur de 18% par rapport au prix moyen fournisseur habituel (7.20 MAD).', 'PURCHASE_ORDER', 2, 'NOUVEAU', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', 'AI_AGENT', 0),
('RETARD_LIVRAISON', 'MOYENNE', 'Risque de rupture fournisseur', 'Fournisseur Souss Céréales SA présente un retard moyen de livraison de 4 jours sur les 2 dernières commandes.', 'SUPPLIER', 2, 'NOUVEAU', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 'AI_AGENT', 0);
