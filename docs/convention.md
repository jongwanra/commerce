# Convention

## Database Schema Convention

### 1. Table Naming

- 테이블 이름은 단수형으로 작성합니다.
    - 예시) `user`, `product`
    - 예외: `order`의 경우 Mysql 예약어이므로 `orders`로 작성합니다.
- 테이블 이름은 소문자와 밑줄(`_`)을 사용하여 작성합니다. (snake_case)
    - 예시) `order_line`, `user_coupon`

## Application Naming Convention

### 1. Class Naming

- 클래스 이름은 파스칼 케이스(PascalCase)를 사용하여 작성합니다.
    - 예시) `UserCouponIssueProcessor`
    - 잘못된 예시) `userCouponIssueProcessor`
- 약어는 앞글자만 대문자로 작성합니다.
    - 예시) `HttpRequestHandler`
    - 잘못된 예시) `HTTPRequestHandler`

