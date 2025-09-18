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

- [상세 보기](https://petstore.swagger.io/?url=https://raw.githubusercontent.com/jongwanra/commerce/jongwanra/impl/docs/api-spec.json)

---

#### API Specification 파일 생성 방법

1. 로컬 환경에서 CommerceApplication을 실행합니다.
2. 아래 명령어를 통해서 api-spec.json 파일을 생성합니다.
    ```shell
      curl http://localhost:8080/v3/api-docs/Commerce > docs/api-spec.json
    ```

### Entity Relational Diagram

- [상세 보기](docs/erd.md)
