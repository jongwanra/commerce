# Application Convention

## Naming Convention

### 1. Class Naming

- 클래스 이름은 파스칼 케이스(PascalCase)를 사용하여 작성합니다.
    - 예시) `UserCouponIssueProcessor`
    - 잘못된 예시) `userCouponIssueProcessor`
- 약어는 앞글자만 대문자로 작성합니다.
    - 예시) `HttpRequestHandler`
    - 잘못된 예시) `HTTPRequestHandler`

## Domain Model Convention

- Domain Model은 `record`를 사용하여 작성합니다.
    - record는 `Immutable Object`를 구현하기에 가장 간단하면서, 안전한 방법이라고 생각합니다.
