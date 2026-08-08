-- =============================================================================
-- NovaERP V4: Seed Initial Frontend Clients
-- =============================================================================

INSERT INTO clients (code, name, company_name, email, phone, address, city, country, credit_limit, status, created_at, updated_at, created_by, version)
VALUES 
    ('CLI-0001', 'Société Atlas Distribution', 'Société Atlas Distribution', 'contact@atlas-dist.ma', '0522334455', 'Zone industrielle, Casablanca', 'Casablanca', 'Morocco', 50000.00, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '45 days', CURRENT_TIMESTAMP - INTERVAL '5 days', 'SYSTEM', 0),
    ('CLI-0002', 'Marjane Market Kenitra', 'Marjane Holding', 'achats@marjane.ma', '0537778899', 'Av. Mohammed V, Kénitra', 'Kénitra', 'Morocco', 100000.00, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '40 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 'SYSTEM', 0),
    ('CLI-0003', 'Épicerie Al Baraka', 'Al Baraka SARL', NULL, '0661234567', 'Quartier Saknia, Kénitra', 'Kénitra', 'Morocco', 10000.00, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '20 days', CURRENT_TIMESTAMP - INTERVAL '20 days', 'SYSTEM', 0),
    ('CLI-0004', 'Café Restaurant Océan', 'Océan Restauration', 'ocean.resto@gmail.com', '0668889900', 'Corniche, Mehdia', 'Mehdia', 'Morocco', 15000.00, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '1 day', 'SYSTEM', 0)
ON CONFLICT (code) DO NOTHING;
