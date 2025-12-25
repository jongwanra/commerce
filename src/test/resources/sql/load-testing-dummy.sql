# 부하 테스트를 위한 사용자 3,000명을 생성합니다.
DELIMITER $$

CREATE PROCEDURE insert_dummy_users()
BEGIN
    DECLARE i INT DEFAULT 1;
    -- 비밀번호는 'password123'을 BCrypt로 암호화한 예시값입니다.
    DECLARE dummy_password VARCHAR(255) DEFAULT '$2a$10$8.UnVuG9HHgffUDAlk8q6uy57sZ.NlH.uU4XzS379tP.6f5iWjZ/a';

    WHILE i <= 3000
        DO
            INSERT INTO hhplus.user (email,
                                     encrypted_password,
                                     status,
                                     created_at,
                                     modified_at)
            VALUES (CONCAT('user', i, '@hhplus.com'),
                    dummy_password,
                    'ACTIVE',
                    NOW(),
                    NOW());
            SET i = i + 1;
        END WHILE;
END$$

DELIMITER ;

-- 실행
CALL insert_dummy_users();

-- 결과 확인
SELECT COUNT(*)
FROM hhplus.user;


# 쿠폰을 발급합니다. (수량: 1,000)
INSERT INTO hhplus.coupon (name, stock, expired_at, discount_type, discount_amount)
values ('이벤트! 상품 50%할인 쿠폰', 1000, '2025-09-20 14:12:30', 'PERCENT', 50.00);
