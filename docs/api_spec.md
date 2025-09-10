# Commerce API Specification

## 공유 사항

- Database + Cookie 기반으로 인증을 처리합니다.
- 일시는 전부 UTC를 기준으로 제공합니다.

### Common Response Format

**전체 API에 대해 응답값 제공**시 아래의 CommerceResponse를 감싸서 제공합니다.

- CommerceResponse\<T>
    - success: boolean (not null)
    - commerceCode: string (nullable)
    - errorMessage: string (nullable)
    - data: T (nullable)

### Common Cursor Pagination Format

- CursorPagination\<E>
    - totalCount: number (not null) - 전체 항목 수
    - hasNext: boolean (not null) - 다음 페이지 존재 여부
    - items: E[] (not null) - 항목 목록

---

## User

### 회원 가입

- Method: POST
- Path: /api/v1/users
- Request Body: object
    - email: string(not null)
    - password: string(not null)
- Response Body: CommerceResponse\<void>

## Auth

### 로그인

- Method: POST
- Path: /api/v1/auth/login
- Request Body: object
    - email: string(not null)
    - password: string(not null)
- Response Body: CommerceResponse\<void>
- Response Header:
    - Set-Cookie: COMMERCE_SESSIONID={sessionId}; Path=/; HttpOnly; SameSite=Lax; Secure=true; Max-Age=86400

### 로그아웃

- Method: POST
- Path: /api/v1/auth/logout
- Response Body: CommerceResponse\<void>
- Response Header:
    - Set-Cookie: COMMERCE_SESSIONID=; Path=/; HttpOnly; SameSite=Lax; Secure=true; Max-Age=0

## Cash

### 잔액 조회

- Method: GET
- Path: /api/v1/me/cash
- Response Body:
    - balance: number (not null) - 현재 잔액
    - modifiedAt: Date (not null) - 마지막 잔액 변경 일시

### 잔액 충전

- Method: POST
- Path: /api/v1/me/cash/charge
- Request Body:
    - amount: number (not null)
- CommerceResponse\<void>

## Product

### 상품 상세 조회

- Method: GET
- Path: /api/v1/products/{productId}
    - Path Variables:
        - productId: number (not null) - 상품 고유 식별자
- Response Body: CommerceResponse<ProductDetailResponse>
    - id: number (not null) - 상품 고유 식별자
    - name: string (not null) - 상품명
    - price: number (not null) - 상품 가격
    - stock: number (not null) - 상품 재고
    - createdAt: Date (not null) - 상품 생성 일시

### 인기 상품 목록 조회

- Summary: 최근 n일간 가장 많이 팔린 상품 목록 조회
- Method: GET
- Path: /api/v1/products/popular
    - Query Parameters:
        - sort: string (nullable, default: "sales_count") - 정렬 기준 (예: "sales_count")
        - days: number (nullable, default: 3) - 최근 n일간
        - limit: number (nullable, default: 5, max: 30) - 조회할 상품 수 (최대 30)
- Response Body: CommerceResponse<ProductSummaryResponse[]>
    - id: number (not null) - 상품 고유 식별자
    - name: string (not null) - 상품명
    - price: number (not null) - 상품 가격
    - createdAt: Date (not null) - 상품 생성 일시

## UserCoupon

### 쿠폰 발급

- Method: POST
- Path: /api/v1/me/coupons/{couponId}
    - Path Variables:
        - couponId: number (not null) - 쿠폰 고유 식별자
- Response Body: CommerceResponse\<void>

### 내 쿠폰 목록 조회

- Method: GET
- Path: /api/v1/me/coupons
    - Query Parameters:
        - lastId: number (not null) - items 중 마지막 항목의 고유 식별자, 첫 페이지: 0
        - size: number (not null, min: 1, max: 50) - 한 번 조회시 페이지 크기
- Response Body: CommerceResponse<CursorPagination<CouponSummaryResponse[]>>
    - id: number (not null) - 사용자 쿠폰 고유 식별자
    - couponId: number (not null) - 쿠폰 고유 식별자
    - name: string (not null) - 쿠폰명
    - discountType: CouponDiscountType (not null) - 쿠폰 할인 유형
        - PERCENTAGE: 백분율 할인
        - AMOUNT: 금액 할인
    - discountValue: number (not null) - 할인값
    - status UserCouponStatus (not null) - 사용자 쿠폰 상태
        - AVAILABLE: 이용 가능한
        - USED: 사용된
    - issuedAt: Date (not null) - 쿠폰 발급 일시
    - expiredAt: Date (null) - 쿠폰 만료 일시, null일 경우 무기한 쿠폰

## Order

### 상품 주문

- Method: POST
- Path: /api/v1/me/orders
- Request Body: object[]
    - productId: number (not null) - 상품 고유 식별자
    - orderQuantity: number (not null) - 주문 수량
- Response Body: CommerceResponse\<void>

## Payment

### 결제

- Method: POST
- Path: /api/v1/me/payments
- Request Body: object
    - orderId: number (not null) - 주문 고유 식별자
    - couponId: number (nullable) - 쿠폰을 사용 안하고 결제할 경우 null
    - expectedPaymentAmount: number (not null) - 결제 시, 클라이언트/서버 간 금액 일치 여부 확인
- Response Body: CommerceResponse\<void>



