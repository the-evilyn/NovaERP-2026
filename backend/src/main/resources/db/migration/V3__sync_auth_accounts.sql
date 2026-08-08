-- =============================================================================
-- NovaERP V3: Seed Frontend Default Users and Sync Roles
-- =============================================================================

-- Add username column to users table if not present for frontend compatibility
ALTER TABLE users ADD COLUMN IF NOT EXISTS username VARCHAR(100);
UPDATE users SET username = 'admin' WHERE email = 'admin@novaerp.com' AND username IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- Seed Salma (Admin) - Password: Password@123 ($2a$12$0vHq/5d6j2p5WomBhyVz6Of634eFq5z1iWbB11Y6P2R9q8Kz.1gma)
INSERT INTO users (username, email, password, first_name, last_name, phone, status, created_at, created_by, version)
VALUES (
    'salma',
    'salma@novaerp.ma',
    '$2a$12$0vHq/5d6j2p5WomBhyVz6Of634eFq5z1iWbB11Y6P2R9q8Kz.1gma',
    'Salma',
    'Architect',
    '+212-611-223344',
    'ACTIVE',
    CURRENT_TIMESTAMP - INTERVAL '60 days',
    'SYSTEM',
    0
)
ON CONFLICT (email) DO NOTHING;

-- Seed Demo (User) - Password: Password@123 ($2a$12$0vHq/5d6j2p5WomBhyVz6Of634eFq5z1iWbB11Y6P2R9q8Kz.1gma)
INSERT INTO users (username, email, password, first_name, last_name, phone, status, created_at, created_by, version)
VALUES (
    'demo',
    'demo@novaerp.ma',
    '$2a$12$0vHq/5d6j2p5WomBhyVz6Of634eFq5z1iWbB11Y6P2R9q8Kz.1gma',
    'Demo',
    'Employee',
    '+212-622-334455',
    'ACTIVE',
    CURRENT_TIMESTAMP - INTERVAL '30 days',
    'SYSTEM',
    0
)
ON CONFLICT (email) DO NOTHING;

-- Map Salma to ROLE_ADMIN
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'salma@novaerp.ma' AND r.name = 'ROLE_ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Map Demo to ROLE_EMPLOYEE
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'demo@novaerp.ma' AND r.name = 'ROLE_EMPLOYEE'
ON CONFLICT (user_id, role_id) DO NOTHING;
