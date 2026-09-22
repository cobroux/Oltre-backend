ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(255) UNIQUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS password VARCHAR(255);

-- Mot de passe par défaut : "admin123" hashé BCrypt
UPDATE users SET
    email = 'admin@oltre.app',
    password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh2S'
WHERE id = 1;