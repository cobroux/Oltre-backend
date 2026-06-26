CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    age INT,
    birth_date DATE
);

CREATE TABLE IF NOT EXISTS expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    expenses_name VARCHAR(255) NOT NULL,
    amount INT,
    rec_type VARCHAR(50),
    start_date DATE,
    end_date DATE,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);