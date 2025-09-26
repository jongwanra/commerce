INSERT INTO user (email, encrypted_password, status)
VALUES ('user.a@gmail.com', 'encrypted_password_a', 'ACTIVE'),
       ('user.b@gmail.com', 'encrypted_password_b', 'ACTIVE'),
       ('user.c@gmail.com', 'encrypted_password_c', 'ACTIVE'),
       ('user.d@gmail.com', 'encrypted_password_d', 'ACTIVE');

INSERT INTO cash (user_id, balance)
SELECT u.id, 0.00
FROM user u
WHERE u.email IN ('user.a@gmail.com', 'user.b@gmail.com', 'user.c@gmail.com');


INSERT INTO cash(user_id, balance)
SELECT u.id, 100000.00
FROM user u
WHERE u.email IN ('user.d@gmail.com');
