INSERT INTO user (email, encrypted_password, status)
VALUES ('user.a@gmail.com', 'encrypted_password_a', 'ACTIVE'),
       ('user.b@gmail.com', 'encrypted_password_b', 'ACTIVE'),
       ('user.c@gmail.com', 'encrypted_password_c', 'ACTIVE');

INSERT INTO cash (user_id, balance)
VALUES (1, 0.00),
       (2, 0.00),
       (3, 0.00);
