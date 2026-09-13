INSERT INTO users (name, email, password_hash, role, created_at, active)
SELECT
    'Administrador',
    'admin@helpdesk.local',
    '$2a$12$zUvSd9SszKoZnuoHfPxrA.LDYSE/Fc2d9xFVIg0Fbl4J3TX2SSWpm',
    'ADMIN',
    CURRENT_TIMESTAMP,
    TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE LOWER(email) = 'admin@helpdesk.local'
);
