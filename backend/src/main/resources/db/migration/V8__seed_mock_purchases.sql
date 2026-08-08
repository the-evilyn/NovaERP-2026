-- =============================================================================
-- NovaERP V8: Seed Mock Purchases (Achats)
-- =============================================================================

-- Purchase 1: RECUE (Received)
INSERT INTO purchase_orders (order_number, supplier_id, warehouse_id, status, order_date, expected_delivery_date, subtotal, tax_rate, tax_amount, total_amount, notes, created_at, updated_at, created_by, version)
SELECT 'ACH-2026-001', s.id, 1, 'RECEIVED', CURRENT_DATE - 30, CURRENT_DATE - 25, 12750.00, 20.00, 2550.00, 15300.00, 'Approvisionnement mensuel huile', CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '25 days', 'SYSTEM', 0
FROM suppliers s WHERE s.code = 'FRN-0001'
LIMIT 1;

INSERT INTO purchase_order_items (purchase_order_id, product_id, quantity_ordered, quantity_received, unit_price, subtotal, tax_rate, tax_amount, total_amount)
SELECT po.id, p.id, 150.00, 150.00, 85.00, 12750.00, 20.00, 2550.00, 15300.00
FROM purchase_orders po, products p
WHERE po.order_number = 'ACH-2026-001' AND p.sku = 'HUI-005';

-- Purchase 2: EN_ATTENTE (Pending)
INSERT INTO purchase_orders (order_number, supplier_id, warehouse_id, status, order_date, expected_delivery_date, subtotal, tax_rate, tax_amount, total_amount, notes, created_at, updated_at, created_by, version)
SELECT 'ACH-2026-002', s.id, 1, 'PENDING_APPROVAL', CURRENT_DATE - 5, CURRENT_DATE + 3, 3600.00, 20.00, 720.00, 4320.00, 'Commande sucre raffinerie', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '5 days', 'SYSTEM', 0
FROM suppliers s WHERE s.code = 'FRN-0002'
LIMIT 1;

INSERT INTO purchase_order_items (purchase_order_id, product_id, quantity_ordered, quantity_received, unit_price, subtotal, tax_rate, tax_amount, total_amount)
SELECT po.id, p.id, 200.00, 0.00, 18.00, 3600.00, 20.00, 720.00, 4320.00
FROM purchase_orders po, products p
WHERE po.order_number = 'ACH-2026-002' AND p.sku = 'SUC-002';
