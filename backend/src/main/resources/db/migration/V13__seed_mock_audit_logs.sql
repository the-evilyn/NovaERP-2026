-- =============================================================================
-- NovaERP V13: Seed Mock Audit Logs (Journal d'audit)
-- =============================================================================

INSERT INTO audit_logs (user_id, user_name, action, entity_type, entity_id, details, ip_address, timestamp, created_at, version)
SELECT u.id, u.first_name || ' ' || u.last_name, 'CONNEXION', 'USER', u.id, 'Authentification réussie au portail NovaERP', '192.168.1.50', CURRENT_TIMESTAMP - INTERVAL '4 hours', CURRENT_TIMESTAMP - INTERVAL '4 hours', 0
FROM users u WHERE u.email = 'admin@novaerp.ma'
LIMIT 1;

INSERT INTO audit_logs (user_id, user_name, action, entity_type, entity_id, details, ip_address, timestamp, created_at, version)
SELECT u.id, u.first_name || ' ' || u.last_name, 'CREATION', 'CLIENT', 1, 'Création de la fiche client LabelVie SA', '192.168.1.50', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days', 0
FROM users u WHERE u.email = 'admin@novaerp.ma'
LIMIT 1;

INSERT INTO audit_logs (user_id, user_name, action, entity_type, entity_id, details, ip_address, timestamp, created_at, version)
SELECT u.id, u.first_name || ' ' || u.last_name, 'VALIDATION', 'SALES_ORDER', 1, 'Validation et expédition de la commande VTE-2026-001', '192.168.1.50', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', 0
FROM users u WHERE u.email = 'admin@novaerp.ma'
LIMIT 1;
