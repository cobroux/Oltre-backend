ALTER TABLE users
  ADD COLUMN push_appt_enabled  BOOLEAN NOT NULL DEFAULT TRUE,
  ADD COLUMN push_appt_minutes  INT     NOT NULL DEFAULT 60,
  ADD COLUMN push_tasks_enabled BOOLEAN NOT NULL DEFAULT TRUE,
  ADD COLUMN push_tasks_time    TIME    NOT NULL DEFAULT '08:00:00',
  ADD COLUMN push_coach_enabled BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE push_subscriptions (
  id         BIGINT NOT NULL AUTO_INCREMENT,
  user_id    BIGINT NOT NULL,
  endpoint   VARCHAR(512) NOT NULL,
  p256dh     VARCHAR(255) NOT NULL,
  auth       VARCHAR(255) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_push_subscriptions_endpoint (endpoint),
  CONSTRAINT fk_push_subscriptions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE push_sent (
  user_id   BIGINT NOT NULL,
  dedup_key VARCHAR(255) NOT NULL,
  sent_at   DATETIME(6) NOT NULL,
  PRIMARY KEY (user_id, dedup_key),
  CONSTRAINT fk_push_sent_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
