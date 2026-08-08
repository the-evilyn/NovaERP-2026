-- =============================================================================
-- NovaERP V6: Seed Mock Warehouses & Stock Movements
-- =============================================================================

-- Seed Warehouses
INSERT INTO warehouses (code, name, address, city, country, is_active, created_at, created_by, version)
VALUES 
    ('WH-CAS-01', 'Entrepôt Central Casablanca', 'Zone Industrielle Sidi Bernoussi, Bd Chefchaouni', 'Casablanca', 'Morocco', true, CURRENT_TIMESTAMP - INTERVAL '90 days', 'SYSTEM', 0),
    ('WH-TNG-01', 'Dépôt Régional Tanger', 'Zone Franche de Tanger, TFZ Lot 45', 'Tanger', 'Morocco', true, CURRENT_TIMESTAMP - INTERVAL '70 days', 'SYSTEM', 0),
    ('WH-RAK-01', 'Dépôt Marrakech', 'Zone Industrielle Sidi Ghanem', 'Marrakech', 'Morocco', true, CURRENT_TIMESTAMP - INTERVAL '50 days', 'SYSTEM', 0)
ON CONFLICT (code) DO NOTHING;

-- Seed Sample Movements
INSERT INTO stock_movements (product_id, source_warehouse_id, target_warehouse_id, movement_type, quantity, unit_cost, reference_type, reference_id, notes, created_at, created_by)
SELECT p.id, NULL, w.id, 'IN_PURCHASE', 150.00, p.purchase_price, 'PURCHASE_ORDER', 'BC-2026-001', 'Réception initiale fournisseur', CURRENT_TIMESTAMP - INTERVAL '40 days', 'SYSTEM'
FROM products p, warehouses w
WHERE p.sku = 'HUI-005' AND w.code = 'WH-CAS-01'
LIMIT 1;

INSERT INTO stock_movements (product_id, source_warehouse_id, target_warehouse_id, movement_type, quantity, unit_cost, reference_type, reference_id, notes, created_at, created_by)
SELECT p.id, w.id, NULL, 'OUT_SALE', 30.00, p.selling_price, 'SALE_ORDER', 'CMD-2026-001', 'Expédition commande client CLI-0001', CURRENT_TIMESTAMP - INTERVAL '20 days', 'SYSTEM'
FROM products p, warehouses w
WHERE p.sku = 'HUI-005' AND w.code = 'WH-CAS-01'
LIMIT 1;

INSERT INTO stock_movements (product_id, source_warehouse_id, target_warehouse_id, movement_type, quantity, unit_cost, reference_type, reference_id, notes, created_at, created_by)
SELECT p.id, NULL, w.id, 'ADJUSTMENT_IN', 10.00, p.purchase_price, 'INVENTORY_ADJUSTMENT', 'INV-2026-01', 'Régularisation inventaire physique', CURRENT_TIMESTAMP - INTERVAL '5 days', 'SYSTEM'
FROM products p, warehouses w
WHERE p.sku = 'SUC-002' AND w.code = 'WH-CAS-01'
LIMIT 1;
