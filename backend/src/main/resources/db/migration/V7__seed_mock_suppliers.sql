-- =============================================================================
-- NovaERP V7: Seed Mock Suppliers (Fournisseurs)
-- =============================================================================

INSERT INTO suppliers (code, name, email, phone, address, city, country, ice, is_active, created_at, updated_at, created_by, version)
VALUES 
    ('FRN-0001', 'Huileries du Souss SA', 'contact@huileries-souss.ma', '0528221100', 'Zone Industrielle Anza, Agadir', 'Agadir', 'Morocco', '001589342000045', true, CURRENT_TIMESTAMP - INTERVAL '120 days', CURRENT_TIMESTAMP - INTERVAL '10 days', 'SYSTEM', 0),
    ('FRN-0002', 'Cosumar Raffinerie SA', 'commercial@cosumar.co.ma', '0522241515', 'Route Nationale 1, Sidi Bennour', 'Sidi Bennour', 'Morocco', '002498112000078', true, CURRENT_TIMESTAMP - INTERVAL '115 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 'SYSTEM', 0),
    ('FRN-0003', 'Grands Moulins du Maghreb', 'ventes@gmm.ma', '0522304050', 'Boulevard Moulay Ismail, Casablanca', 'Casablanca', 'Morocco', '003112445000091', true, CURRENT_TIMESTAMP - INTERVAL '100 days', CURRENT_TIMESTAMP - INTERVAL '12 days', 'SYSTEM', 0),
    ('FRN-0004', 'Eaux Minérales d''Oulmès', 'commandes@oulmes.ma', '0537881020', 'Route de Rommani, Oulmès', 'Oulmès', 'Morocco', '004781223000012', true, CURRENT_TIMESTAMP - INTERVAL '90 days', CURRENT_TIMESTAMP - INTERVAL '5 days', 'SYSTEM', 0),
    ('FRN-0005', 'Laboratoires Novachim Maroc', 'info@novachim.ma', '0523321144', 'Parc Industriel Sapino, Nouaceur', 'Casablanca', 'Morocco', '005991823000033', true, CURRENT_TIMESTAMP - INTERVAL '60 days', CURRENT_TIMESTAMP - INTERVAL '20 days', 'SYSTEM', 0)
ON CONFLICT (code) DO NOTHING;
