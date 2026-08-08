-- =============================================================================
-- NovaERP V5: Seed Mock Categories and Products
-- =============================================================================

-- Ensure categories exist
INSERT INTO categories (code, name, description, is_active, created_at, created_by, version)
VALUES 
    ('CAT-ALIM', 'Alimentation', 'Produits alimentaires de base', true, CURRENT_TIMESTAMP - INTERVAL '60 days', 'SYSTEM', 0),
    ('CAT-BOIS', 'Boissons', 'Boissons et eaux minérales', true, CURRENT_TIMESTAMP - INTERVAL '60 days', 'SYSTEM', 0),
    ('CAT-ENTR', 'Entretien', 'Produits d''entretien ménager et industriel', true, CURRENT_TIMESTAMP - INTERVAL '60 days', 'SYSTEM', 0)
ON CONFLICT (code) DO NOTHING;

-- Seed Products
INSERT INTO products (category_id, sku, name, purchase_price, selling_price, min_stock_level, max_stock_level, unit_of_measure, status, created_at, updated_at, created_by, version)
VALUES 
    ((SELECT id FROM categories WHERE code = 'CAT-ALIM'), 'HUI-005', 'Huile de table 5L', 85.00, 105.00, 30.00, 500.00, 'UNITE', 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '50 days', CURRENT_TIMESTAMP - INTERVAL '1 day', 'SYSTEM', 0),
    ((SELECT id FROM categories WHERE code = 'CAT-ALIM'), 'SUC-002', 'Sucre 2kg', 18.00, 24.00, 25.00, 500.00, 'UNITE', 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '50 days', CURRENT_TIMESTAMP, 'SYSTEM', 0),
    ((SELECT id FROM categories WHERE code = 'CAT-ALIM'), 'FAR-010', 'Farine 10kg', 55.00, 72.00, 15.00, 300.00, 'UNITE', 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '48 days', CURRENT_TIMESTAMP - INTERVAL '3 days', 'SYSTEM', 0),
    ((SELECT id FROM categories WHERE code = 'CAT-BOIS'), 'EAU-006', 'Eau minérale pack 6x1.5L', 22.00, 30.00, 20.00, 400.00, 'UNITE', 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '35 days', CURRENT_TIMESTAMP, 'SYSTEM', 0),
    ((SELECT id FROM categories WHERE code = 'CAT-ENTR'), 'DET-003', 'Détergent 3L', 28.00, 39.00, 10.00, 200.00, 'UNITE', 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '15 days', 'SYSTEM', 0),
    ((SELECT id FROM categories WHERE code = 'CAT-BOIS'), 'THE-500', 'Thé vert 500g', 40.00, 58.00, 20.00, 300.00, 'UNITE', 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '60 days', CURRENT_TIMESTAMP - INTERVAL '55 days', 'SYSTEM', 0)
ON CONFLICT (sku) DO NOTHING;

-- Initialize Stock records for seeded products
INSERT INTO stock (product_id, warehouse_id, quantity_on_hand, quantity_allocated, quantity_available, created_at, created_by, version)
SELECT p.id, 1, 
    CASE 
        WHEN p.sku = 'HUI-005' THEN 120.00
        WHEN p.sku = 'SUC-002' THEN 8.00
        WHEN p.sku = 'FAR-010' THEN 60.00
        WHEN p.sku = 'EAU-006' THEN 3.00
        WHEN p.sku = 'DET-003' THEN 45.00
        WHEN p.sku = 'THE-500' THEN 90.00
        ELSE 0.00
    END,
    0.00,
    CASE 
        WHEN p.sku = 'HUI-005' THEN 120.00
        WHEN p.sku = 'SUC-002' THEN 8.00
        WHEN p.sku = 'FAR-010' THEN 60.00
        WHEN p.sku = 'EAU-006' THEN 3.00
        WHEN p.sku = 'DET-003' THEN 45.00
        WHEN p.sku = 'THE-500' THEN 90.00
        ELSE 0.00
    END,
    p.created_at,
    'SYSTEM',
    0
FROM products p
WHERE p.sku IN ('HUI-005', 'SUC-002', 'FAR-010', 'EAU-006', 'DET-003', 'THE-500')
ON CONFLICT (product_id, warehouse_id) DO NOTHING;
