-- =============================================================================
-- NovaERP V9: Seed Mock Sales (Ventes)
-- =============================================================================

-- Sale 1: LIVREE (Delivered)
INSERT INTO sales_orders (order_number, client_id, warehouse_id, status, order_date, delivery_date, subtotal, tax_rate, tax_amount, total_amount, notes, created_at, updated_at, created_by, version)
SELECT 'VTE-2026-001', c.id, 1, 'LIVREE', CURRENT_DATE - 15, CURRENT_DATE - 14, 23000.00, 20.00, 4600.00, 27600.00, 'Livraison Supermarché LabelVie', CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '14 days', 'SYSTEM', 0
FROM clients c WHERE c.code = 'CLI-0001'
LIMIT 1;

INSERT INTO sales_order_items (sales_order_id, product_id, quantity_ordered, quantity_shipped, unit_price, subtotal, tax_rate, tax_amount, total_amount)
SELECT so.id, p.id, 200.00, 200.00, 115.00, 23000.00, 20.00, 4600.00, 27600.00
FROM sales_orders so, products p
WHERE so.order_number = 'VTE-2026-001' AND p.sku = 'HUI-005';

-- Sale 2: COMMANDE (Confirmed Order)
INSERT INTO sales_orders (order_number, client_id, warehouse_id, status, order_date, delivery_date, subtotal, tax_rate, tax_amount, total_amount, notes, created_at, updated_at, created_by, version)
SELECT 'VTE-2026-002', c.id, 1, 'COMMANDE', CURRENT_DATE - 3, CURRENT_DATE + 2, 7000.00, 20.00, 1400.00, 8400.00, 'Commande Biscuiterie Henry''s', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days', 'SYSTEM', 0
FROM clients c WHERE c.code = 'CLI-0002'
LIMIT 1;

INSERT INTO sales_order_items (sales_order_id, product_id, quantity_ordered, quantity_shipped, unit_price, subtotal, tax_rate, tax_amount, total_amount)
SELECT so.id, p.id, 200.00, 0.00, 26.00, 5200.00, 20.00, 1040.00, 6240.00
FROM sales_orders so, products p
WHERE so.order_number = 'VTE-2026-002' AND p.sku = 'SUC-002';

INSERT INTO sales_order_items (sales_order_id, product_id, quantity_ordered, quantity_shipped, unit_price, subtotal, tax_rate, tax_amount, total_amount)
SELECT so.id, p.id, 200.00, 0.00, 9.00, 1800.00, 20.00, 360.00, 2160.00
FROM sales_orders so, products p
WHERE so.order_number = 'VTE-2026-002' AND p.sku = 'FAR-001';
