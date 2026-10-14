-- Schéma initial, repris tel que Hibernate l'avait généré (ddl-auto=update) :
-- Flyway n'a jamais tourné avant cette migration (il manquait
-- spring-boot-starter-flyway avec Spring Boot 4). Les bases existantes sont
-- marquées à cette version (baseline-on-migrate) sans la rejouer.
-- Pas de données de démo ni de compte par défaut.

CREATE TABLE users (
  id         BIGINT NOT NULL AUTO_INCREMENT,
  age        INT DEFAULT NULL,
  birth_date DATE DEFAULT NULL,
  email      VARCHAR(255) DEFAULT NULL,
  password   VARCHAR(255) DEFAULT NULL,
  username   VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_email (email)
);

CREATE TABLE appointments (
  id            BIGINT NOT NULL AUTO_INCREMENT,
  appt_date     DATE NOT NULL,
  appt_end_time TIME DEFAULT NULL,
  appt_time     TIME DEFAULT NULL,
  appt_type     ENUM('AUTRE','MEDICAL','PERSO','SPORT','TRAVAIL') DEFAULT NULL,
  description   VARCHAR(255) DEFAULT NULL,
  location      VARCHAR(255) DEFAULT NULL,
  source        ENUM('COACH_PLAN','GARMIN','MANUAL') NOT NULL,
  source_ref    VARCHAR(255) DEFAULT NULL,
  title         VARCHAR(255) NOT NULL,
  user_id       BIGINT DEFAULT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_appointments_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE coach_plans (
  id           BIGINT NOT NULL AUTO_INCREMENT,
  analysis     LONGTEXT NOT NULL,
  days_json    LONGTEXT NOT NULL,
  generated_at DATETIME(6) NOT NULL,
  user_id      BIGINT DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_coach_plans_user (user_id),
  CONSTRAINT fk_coach_plans_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE expenses (
  id            BIGINT NOT NULL AUTO_INCREMENT,
  amount        DECIMAL(10,2) DEFAULT NULL,
  end_date      DATE DEFAULT NULL,
  expenses_name VARCHAR(255) DEFAULT NULL,
  rec_type      ENUM('Daily','Monthly','Yearly') DEFAULT NULL,
  start_date    DATE DEFAULT NULL,
  user_id       BIGINT DEFAULT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_expenses_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE meals (
  id            BIGINT NOT NULL AUTO_INCREMENT,
  calories      INT DEFAULT NULL,
  carbs_g       DOUBLE DEFAULT NULL,
  fat_g         DOUBLE DEFAULT NULL,
  meal_date     DATE DEFAULT NULL,
  meal_descript VARCHAR(255) DEFAULT NULL,
  meal_name     VARCHAR(255) NOT NULL,
  meal_type     ENUM('COLLATION','DEJEUNER','DINER','PETIT_DEJ') DEFAULT NULL,
  off_barcode   VARCHAR(255) DEFAULT NULL,
  protein_g     DOUBLE DEFAULT NULL,
  quantity_g    DOUBLE DEFAULT NULL,
  user_id       BIGINT DEFAULT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_meals_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE sport_goals (
  id          BIGINT NOT NULL AUTO_INCREMENT,
  event_date  DATE NOT NULL,
  goal_type   ENUM('Marathon','Other','SemiMarathon','TenK','Triathlon') NOT NULL,
  name        VARCHAR(255) NOT NULL,
  target_time VARCHAR(255) DEFAULT NULL,
  user_id     BIGINT DEFAULT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_sport_goals_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE tasks (
  id             BIGINT NOT NULL AUTO_INCREMENT,
  tasks_date     DATE DEFAULT NULL,
  tasks_name     VARCHAR(255) DEFAULT NULL,
  tasks_priority ENUM('HIGH','LOW','MEDIUM') DEFAULT NULL,
  tasks_status   ENUM('DONE','PENDING') DEFAULT NULL,
  tasks_type     ENUM('AUTRE','PERSO','SPORT','TRAVAIL') DEFAULT NULL,
  user_id        BIGINT DEFAULT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_tasks_user FOREIGN KEY (user_id) REFERENCES users (id)
);
