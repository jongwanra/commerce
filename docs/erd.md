# Entity Relational Diagram (ERD)

updatedAt: 2025.08.22

- Link: https://dbdiagram.io/d/commerce-68a57e58ec93249d1e69f6d4

![erd](media/erd_250829.png)

## Indexing

### Cash

- user_id (unique)

## Naming Convention

### 고민 사항

#### _price vs _amount

상품 가격과 주문가에 대한 column suffix로 어떤 것을 쓸지에 대해 고민했습니다.

- _price
    - 상품 자체의 속성으로 가격을 나타낼 때 사용합니다. (상품 가격, 할인 가격, ...)
- _amount
    - 주문과 관련된 금액을 나타낼 때 사용합니다. (주문 금액, 결제 금액, 환불 금액...)

