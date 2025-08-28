# Commerce

## Getting Started

### Prerequisites

#### Running Docker Containers

`local` profile 로 실행하기 위하여 인프라가 설정되어 있는 Docker 컨테이너를 실행해주셔야 합니다.

```bash
docker-compose up -d
```


### System Architecture
- [상세 보기](docs/system_architecture.md)

### API Specification
- [상세 보기](docs/api_spec.md)

### Entity Relational Diagram
- [상세 보기](docs/erd.md)


### Price vs Amount
상품 가격과 주문가에 대한 column suffix로 어떤 것을 쓸지에 대해 고민했습니다.

- Price
    -  상품 자체의 속성으로 가격을 나타낼 때 사용합니다. (상품 가격, 할인 가격, ...)
- Amount
    - 주문과 관련된 금액을 나타낼 때 사용합니다. (주문 금액, 결제 금액, 환불 금액...)
