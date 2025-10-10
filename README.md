# Commerce

> 이커머스 플랫폼의 핵심 도메인을 구현한 백엔드 애플리케이션입니다.

## 목차

1. [Getting Started](#1-getting-started)
2. [System Architecture](#2-system-architecture)
3. [API Specification](#3-api-specification)
4. [Entity Relational Diagram](#4-entity-relational-diagram)
5. [Convention](#5-convention)
6. [Sequence Diagram](#6-sequence-diagram)
7. [Guides](#7-guides)

---

## 1. Getting Started

아래 순서대로 진행하면 로컬 환경에서 애플리케이션을 실행할 수 있습니다.

### 1-1. Prerequisites

- JDK 17+
- Docker, Docker Compose
- Git
- (선택) IntelliJ IDEA 또는 다른 IDE

### 1-2. Clone & Build

```bash
# 저장소 클론
git clone https://github.com/jongwanra/commerce.git
cd commerce

# 빌드 (테스트 실행 포함)
./gradlew clean build
```

### 1-3. Run Infrastructure (MySQL)

`local` 프로파일로 애플리케이션을 실행하기 위해서는 Docker 기반 인프라(MySQL)가 필요합니다.

```bash
docker-compose up -d
```

- MySQL 접속 정보 (docker-compose.yml 기준)
    - host: localhost, port: 3306
    - database: hhplus
    - username: application, password: application

### 1-4. Run Application

다음 두 방법 중 하나로 애플리케이션을 실행할 수 있습니다. 기본 활성 프로파일은 `local` 입니다.

```bash
# 1) Gradle로 실행
./gradlew bootRun

# 2) JAR로 실행 (빌드 후)
java -jar build/libs/commerce-*.jar
```

- 기본 포트: 8080
- Swagger UI: http://localhost:8080/docs/swagger

### 1-5. API Specification 파일 생성 (옵션)

로컬에서 애플리케이션 실행 후 아래 명령으로 API 스펙을 갱신할 수 있습니다.

```bash
# API Specification 파일 생성
curl http://localhost:8080/v3/api-docs/Commerce > docs/api-spec.json

# 생성 확인
cat docs/api-spec.json | head -n 5
```

### 1-6. Run Tests

```bash
./gradlew test
```

### 1-7. Troubleshooting

- 3306 포트 충돌: 로컬 MySQL이 이미 실행 중이면 중지하거나 `docker-compose.yml`의 포트를 변경하세요.
- 데이터 초기화: 컨테이너/볼륨을 재생성하려면 컨테이너를 내리고 `data/mysql` 디렉터리를 정리한 후 다시 `docker-compose up -d`를 실행하세요.

---

## 2. System Architecture

- [상세 보기](docs/system_architecture.md)

---

## 3. API Specification

- [상세 보기](https://petstore.swagger.io/?url=https://raw.githubusercontent.com/jongwanra/commerce/main/docs/api-spec.json)

---

## 4. Entity Relational Diagram

- [상세 보기](docs/erd.md)

---

## 5. Convention

### 5-1. Application Convention

- [상세 보기](docs/application_convention.md)

### 5-2. Database Convention

- [상세 보기](docs/db_convention.md)

---

## 6. Sequence Diagram

- [상세 보기](docs/sequence_diagram.md)

---

## 7. Guides

- [메세지 기반 외부 시스템 연동 가이드](/docs/guide/external_system_integration_guide.md)

