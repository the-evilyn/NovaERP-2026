-- =============================================================================
-- NovaERP V12: Seed Mock Alerts (Alertes & Notifications)
-- =============================================================================

INSERT INTO alerts (type, title, message, severity, is_read, entity_type, entity_id, created_at, updated_at, created_by, version)
VALUES
('STOCK_BAS', 'Stock critique détecté', 'Le niveau de stock pour Riz Parfumé 5kg (RIZ-003) est inférieur au seuil minimal (30 unités restantes).', 'DANGER', false, 'PRODUCT', 3, CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours', 'SYSTEM', 0),
('FACTURE_IMPAYEE', 'Facture en attente de règlement', 'La facture FAC-2026-002 de Marjane Holding (8,400.00 MAD) arrive à échéance prochainement.', 'WARNING', false, 'INVOICE', 2, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', 'SYSTEM', 0),
('SYSTEME', 'Synchronisation ERP réussie', 'La clôture journalière et la consolidation des inventaires ont été effectuées avec succès.', 'INFO', true, 'SYSTEM', NULL, CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days', 'SYSTEM', 0);
