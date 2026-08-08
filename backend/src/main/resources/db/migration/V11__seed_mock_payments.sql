-- =============================================================================
-- NovaERP V11: Seed Mock Payments (Paiements)
-- =============================================================================

INSERT INTO payments (payment_number, client_id, invoice_id, type, method, status, amount, payment_date, reference_number, notes, created_at, updated_at, created_by, version)
SELECT 'REG-2026-001', c.id, inv.id, 'INBOUND_CUSTOMER', 'BANK_TRANSFER', 'CLEARED', 27600.00, CURRENT_DATE - 10, 'VIR-BMCE-9921', 'Règlement total facture FAC-2026-001', CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '10 days', 'SYSTEM', 0
FROM clients c, invoices inv
WHERE c.code = 'CLI-0001' AND inv.invoice_number = 'FAC-2026-001'
LIMIT 1;
