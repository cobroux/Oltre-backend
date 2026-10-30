ALTER TABLE users
  ADD COLUMN terms_accepted_at       DATETIME(6) NULL,
  ADD COLUMN onboarding_completed_at DATETIME(6) NULL;

-- Comptes existants : pas de parcours d'accueil (terms_accepted_at reste NULL).
UPDATE users SET onboarding_completed_at = NOW(6);
