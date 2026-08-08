-- =============================================================================
-- NovaERP V10: Seed Mock Invoices (Factures)
-- =============================================================================

-- Invoice 1: PAYEE
INSERT INTO invoices (invoice_number, client_id, sales_order_id, type, status, issue_date, due_date, subtotal, tax_rate, tax_amount, total_amount, paid_amount, notes, created_at, updated_at, created_by, version)
SELECT 'FAC-2026-001', c.id, so.id, 'STANDARD', 'PAYEE', CURRENT_DATE - 15, CURRENT_DATE + 15, 23000.00, 20.00, 4600.00, 27600.00, 27600.00, 'Facture réglée par virement', CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '10 days', 'SYSTEM', 0
FROM clients c, sales_orders so
WHERE c.code = 'CLI-0001' AND so.order_number = 'VTE-2026-001'
LIMIT 1;

INSERT INTO invoice_items (invoice_id, product_id, quantity, unit_price, subtotal, tax_rate, tax_amount, total_amount)
SELECT inv.id, p.id, 200.00, 115.00, 23000.00, 20.00, 4600.00, 27600.00
FROM invoices inv, products p
WHERE inv.invoice_number = 'FAC-2026-001' AND p.sku = 'HUI-005';

-- Invoice 2: VALIDEE (Pending payment)
INSERT INTO invoices (invoice_number, client_id, sales_order_id, type, status, issue_date, due_date, subtotal, tax_rate, tax_amount, total_amount, paid_amount, notes, created_at, updated_at, created_by, version)
SELECT 'FAC-2026-002', c.id, so.id, 'STANDARD', 'VALIDEE', CURRENT_DATE - 2, CURRENT_DATE + 28, 7000.00, 20.00, 1400.00, 8400.00, 0.00, 'Facture émise en attente de paiement', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 'SYSTEM', 0
FROM clients c, sales_orders so
WHERE c.code = 'CLI-0002' AND so.order_number = 'VTE-2026-002'
LIMIT 1;

INSERT INTO invoice_items (invoice_id, product_id, quantity, unit_price, subtotal, tax_rate, tax_amount, total_amount)
SELECT inv.id, p.id, 200.00, 26.00, 5200.00, 20.00, 1040.00, 6240.00
FROM invoices inv, products p
WHERE inv.invoice_number = 'FAC-2026-002' AND p.sku = 'SUC-002';

INSERT INTO invoice_items (invoice_id, product_id, quantity, unit_price, subtotal, tax_rate, tax_amount, total_amount)
SELECT inv.id, p.id, 200.00, 9.00, 1800.00, 20.00, 360.00, 2160.00
FROM invoices inv, products p
WHERE inv.invoice_number = 'FAC-2026-002' AND p.sku = 'FAR-001';
