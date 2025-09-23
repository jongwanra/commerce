# Database Convention

## Naming Convention

### 1. Table Naming Convention

#### 1-1. 테이블 이름은 단수형으로 작성합니다.

- 예시) `user`, `product`
- 예외: `order`의 경우 Mysql 예약어이므로 `orders`로 작성합니다.

#### 1-2. 테이블 이름은 소문자와 밑줄(`_`)을 사용하여 작성합니다. (snake_case)

- 예시) `order_line`, `user_coupon`

#### 1-3. 보조 인덱스는 `idx_{테이블이름}_{컬럼1}_{컬럼2}...` 형태로 작성합니다.

- 예시) `idx_orders_user_id`

#### 1-4 유니크 인덱스는 `uidx_{테이블이름}_{컬럼1}_{컬럼2}...` 형태로 작성합니다.

- 예시) `uidx_user_email`

### 2. Column Naming Convention

#### 2-1. _price vs _amount

상품 가격과 주문가에 대한 column suffix로 어떤 것을 쓸지에 대해 고민했습니다.

- _price
    - 상품 자체의 속성으로 가격을 나타낼 때 사용합니다. (상품 가격, 할인 가격, ...)
- _amount
    - 주문/결제와 관련된 금액을 나타낼 때 사용합니다. (주문 금액, 결제 금액, 환불 금액...)

#### 2-2. _snapshot

- order_line, user_coupon 등의 테이블에서 `_snapshot` suffix가 붙은 컬럼들이 있습니다.
- 이는 주문 당시 혹은 쿠폰 발급 당시의 정보를 보존하기 위한 용도로 사용됩니다.
- 예를 들어, 상품명이 변경되더라도 주문 당시의 상품명을 유지하기 위해 order_line 테이블에 product_name_snapshot 컬럼이 존재합니다.
