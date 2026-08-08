-- =============================================================================
-- NovaERP Initial Seed Data - Version 1.0.0
-- Roles, Admin Account, Sample Reference Categories
-- =============================================================================

-- Seed Standard RBAC Roles
INSERT INTO roles (name, description, created_at)
VALUES 
    ('ROLE_ADMIN', 'System Administrator with full enterprise access and configuration rights', CURRENT_TIMESTAMP),
    ('ROLE_MANAGER', 'Business Manager with commercial, inventory, and reporting management permissions', CURRENT_TIMESTAMP),
    ('ROLE_EMPLOYEE', 'Standard Employee with operational access to sales, stock movements, and clients', CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- Seed Default Enterprise Admin User
-- Password: Admin@123456 (BCrypt encoded: $2a$12$0vHq/5d6j2p5WomBhyVz6Of634eFq5z1iWbB11Y6P2R9q8Kz.1gma)
INSERT INTO users (email, password, first_name, last_name, phone, status, created_at, created_by, version)
VALUES (
    'admin@novaerp.com',
    '$2a$12$0vHq/5d6j2p5WomBhyVz6Of634eFq5z1iWbB11Y6P2R9q8Kz.1gma',
    'NovaERP',
    'Administrator',
    '+212-600-000000',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    'SYSTEM',
    0
)
ON CONFLICT (email) DO NOTHING;

-- Map Admin to all standard roles
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@novaerp.com'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Seed Baseline Product Categories
INSERT INTO categories (code, name, description, status, created_at, created_by, version)
VALUES 
    ('RAW_MAT', 'Raw Materials', 'Industrial and manufacturing raw components', 'ACTIVE', CURRENT_TIMESTAMP, 'SYSTEM', 0),
    ('FIN_GOODS', 'Finished Goods', 'Manufactured final commercial products', 'ACTIVE', CURRENT_TIMESTAMP, 'SYSTEM', 0),
    ('SPARE_PARTS', 'Spare Parts & Maintenance', 'Machine components, tools, and maintenance supplies', 'ACTIVE', CURRENT_TIMESTAMP, 'SYSTEM', 0),
    ('PACKAGING', 'Packaging Materials', 'Industrial packaging, boxes, and protective wrapping', 'ACTIVE', CURRENT_TIMESTAMP, 'SYSTEM', 0)
ON CONFLICT (code) DO NOTHING;
