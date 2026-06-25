INSERT INTO users (id, username, age, birth_date) 
VALUES (1, 'Admin', 30, '1996-05-12')
ON DUPLICATE KEY UPDATE username=username; 

INSERT INTO expenses (id, expenses_name, amount, rec_type, start_date, user_id)
VALUES (1, 'Netflix', 13, 1, NOW(), 1),
       (2, 'Spotify', 10, 1, NOW(), 1)
ON DUPLICATE KEY UPDATE expenses_name=expenses_name;