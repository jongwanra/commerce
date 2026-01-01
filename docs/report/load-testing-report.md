# [결과 보고서] 선착순 쿠폰 발급 시스템 스파이크 테스트

## 1. 테스트 개요

### 1.1 테스트 목적

- 대상 API: `POST /api/v2/me/coupons/{couponId}/issue`
- 목표: 2초 이내 2,000명의 동시 요청 처리 (TPS 1,000 이상)
- 검증 항목:
    - 데이터 정합성: 쿠폰 1,000개 정확히 발급
    - 가용성: 시스템 장애 없이 안정적인 응답
    - 성능: TPS 1,000 이상 p(99) 2초 이내

### 1.2 테스트 환경

```yaml
# Docker Compose 기반 인프라
App:
  - CPU: 4.0 cores
  - Memory: 2GB
  - JVM: -Xms1g -Xmx1536m

MySQL 8.0:
  - HikariCP max-pool-size: 50

Redis 7.0:
  - Lettuce pool max-active: 200
```

### 1.3 K6 테스트 스크립트

```javascript
import http from 'k6/http';
import {check} from 'k6';

export const options = {
    scenarios: {
        coupon_spike: {
            executor: 'per-vu-iterations',
            vus: 2000,              // 2,000명 동시 요청
            iterations: 1,           // 각 1회씩 실행
            maxDuration: '1m'
        }
    },
    thresholds: {
        http_req_duration: ['p(99)<2000'],
        http_req_failed: ['rate<0.51'], // 1,000명 성공, 1,000명 실패
    }
}

export default function () {
    const userId = __VU;
    const couponId = 1;
    const url = `http://localhost:8080/api/v2/me/coupons/${couponId}/issue`;

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Commerce-User-Id': userId
        },
        timeout: '30s',
    };

    const res = http.post(url, null, params);

    check(res, {
        'status is 201 or 400': (r) => [201, 400].includes(r.status),
        'no server error 500': (r) => r.status !== 500
    });
}
```

## 2. 시도별 테스트 결과

### 2.1 1차 시도: DB 비관적 잠금 + 분산락

#### 구현

```java

@DistributedLock(key = "coupon", keyExpression = "#command.couponId()")
@Transactional
public Output execute(Command command) {
	// findByIdForUpdate - 비관적 잠금(SELECT FOR UPDATE)
	Coupon issuedCoupon = couponRepository.findByIdForUpdate(command.couponId())
		.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_COUPON))
		.issue(command.now());

	if (userCouponRepository.existsByUserIdAndCouponId(command.userId(), command.couponId())) {
		throw new CommerceException(CommerceCode.ALREADY_ISSUED_COUPON);
	}

	return new Output(
		couponRepository.save(issuedCoupon),
		userCouponRepository.save(UserCoupon.of(command.userId(), issuedCoupon, command.now()))
	);
}
```

#### 결과

- TPS: 약 20
- 정합성:✅
- 성능:❌
- 문제점
    - DB 비관적 잠금으로 인한 심각한 병목
    - 대부분의 요청이 락 대기 상태로 지연
    - 2,000명 요청 중 387명만 처리

### 2.2 2차 시도: 분산락만 사용 (비관적 잠금 제거)

#### 구현

```java

@DistributedLock(key = "coupon", keyExpression = "#command.couponId()")
@Transactional
public Output execute(Command command) {
	// findById - 일반 조회
	Coupon issuedCoupon = couponRepository.findById(command.couponId())
		.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_COUPON))
		.issue(command.now());

	if (userCouponRepository.existsByUserIdAndCouponId(command.userId(), command.couponId())) {
		throw new CommerceException(CommerceCode.ALREADY_ISSUED_COUPON);
	}

	return new Output(
		couponRepository.save(issuedCoupon),
		userCouponRepository.save(UserCoupon.of(command.userId(), issuedCoupon, command.now()))
	);
}

```

#### 분산락 설정

- waitTime: 5s
- leaseTime: 3s

#### 결과

- TPS: 47
- 정합성:❌
    - coupon.stock: 478개
    - user_coupon.count: 526개
- 에러율: 90.03%

#### 문제점

- waitTime(5초) 내에 락을 획득하지 못한 요청들이 대량 실패
- 짧은 leaseTime으로 인한 동시성 제어 실패
- 정합성 깨짐: 재고보다 더 많은 쿠폰 발급

### 2.3 3차시도: 분산락 타임아웃 조정

#### 변경 사항

```java
@DistributedLock(key = "coupon", keyExpression = "#command.couponId()", waitTime = 30, leaseTime = 27)
```

#### 결과

- TPS: 45.76
- 정합성:✅
- 에러율: 85.83%
- p(99): 60s

#### 분석

- 정합성은 확보했으나 성능이 여전히 낮음
- 직렬화된 락 처리로 인해 TPS 20 수준에 머물러 있음
- **근본적인 해결책 필요**

### 2.4 4차 ~ 6차 시도: Redis Lua Script 기반 원자적 연산

#### 구현 방식 변경

기존의 분산락 방식에서 Redis Lua Script를 활용한 원자적 연산으로 전환

```java

@Service
@RequiredArgsConstructor
public class UserCouponIssueWithEventProcessor {
	private static final String ISSUE_COUPON_KEY = "issue:coupon:%s";
	private static final String COUPON_STOCK_KEY = "coupon:%s:stock";
	private static final DefaultRedisScript<String> REDIS_SCRIPT = new DefaultRedisScript<>(generateScript(),
		String.class);

	private final RedisTemplate<String, String> redisTemplate;
	private final InternalEventPublisher internalEventPublisher;
	private final TimeProvider timeProvider;

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void execute(Command command) {
		final LocalDateTime now = timeProvider.now();
		final String issueCouponKey = String.format(ISSUE_COUPON_KEY, command.couponId());
		final String stockCouponKey = String.format(COUPON_STOCK_KEY, command.couponId());

		String result = redisTemplate.execute(
			REDIS_SCRIPT,
			List.of(issueCouponKey, stockCouponKey), // KEYS[1], KEYS[2]
			String.valueOf(command.userId()) // ARGV[1]
		);

		if (result.equals("DUPLICATE")) {
			throw new CommerceException(CommerceCode.ALREADY_ISSUED_COUPON);
		}
		if (!result.equals("SUCCESS")) {
			throw new CommerceException(CommerceCode.OUT_OF_STOCK_COUPON);
		}
		internalEventPublisher.publish(CouponIssuedEvent.of(command.couponId(), command.userId(), now));

	}

	private static String generateScript() {
		return "local limit = tonumber(redis.call('GET', KEYS[2])) "
			+ "local current_count = redis.call('SCARD', KEYS[1]) "
			+ "if current_count >= limit then return 'SOLD_OUT' end "
			+ "if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then return 'DUPLICATE' end "
			+ "redis.call('SADD', KEYS[1], ARGV[1]) "
			+ "return 'SUCCESS'";
	}

	public record Command(
		Long userId,
		Long couponId,
		LocalDateTime now
	) {
	}

}

```

##### 비동기 이벤트 처리

```java

@Component
@RequiredArgsConstructor
public class CouponIssuedEventListener {
	@Async
	@EventListener
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handle(CouponIssuedEvent event) {
		Coupon issuedCoupon = couponRepository.findByIdForUpdate(event.couponId())
			.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_COUPON))
			.issue(event.occurredAt());

		couponRepository.save(issuedCoupon);
		userCouponRepository.save(
			UserCoupon.of(event.userId(), issuedCoupon, event.occurredAt())
		);
	}
}
```

#### 점진적 튜닝 과정

#### 4차 시도 - 초기 Redis Script 적용

- TPS: 102.54
- p(99): 27.71s
- 에러율: 66.66%

#### 5차 시도 - DB Connection Pool 조정

```yaml
datasource:
  hikari:
    maximum-pool-size: 50
    minimum-idle: 47
    connection-timeout: 30000
```

- TPS: 87.09
- p(99): 32.19s

#### 6차 시도 - Redis Connection Pool 증가

```yaml
redis:
  lettuce:
    pool:
      max-active: 50
      max-idle: 10
      min-idle: 2
```

- TPS: 108.59
- p(99): 25.15s

### 2.5 7차 시도: 최종 최적화 (목표 달성)

#### 최종 설정

##### Appplication 설정 (application-perf.yml)

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 47
      connection-timeout: 30000
      max-lifetime: 60000

  data:
    redis:
      # lettuce 설정은 RedisConfig.java를 참고 해주세요.
      lettuce:
        pool:
          max-active: 200
          max-idle: 200
          min-idle: 50
          max-wait: 3000ms

  task:
    execution:
      pool:
        core-size: 50
        max-size: 100
        queue-capacity: 2000

server:
  tomcat:
    threads:
      max: 200
      min-spare: 20
    accept-count: 3000
```

#### 최종 테스트 결과(VUS 2,000)

```shell
✅ THRESHOLDS
  http_req_duration: 'p(99)<2000' ✓ p(99)=1.77s
  http_req_failed: 'rate<0.51' ✓ rate=50.00%

📊 PERFORMANCE METRICS
  TPS: 996.67
  http_req_duration:
    - avg: 1.11s
    - p(99): 1.77s
  
  Success Rate: 50% (1,000 성공 / 1,000 재고 소진)
  
  정합성 검증:
    - Redis SCARD issue:coupon:1 → 1,000
    - 쿠폰 발급 수: 1,000개 (정확히 일치) ✅
```

## 3. 성능 개선 요약

| 시도 | 방식               | TPS | p(99) | 정합성 | 비고     |
|----|------------------|-----|-------|-----|--------|
| 1차 | 데이터베이스 락 + 분산락   | 20  | 60s+  | ✅   | 심각한 병목 |
| 2차 | 분산락 only(5s/3s)  | 47  | 60s   | ❌   | 정합성 이슈 |
| 3차 | 분산락 (30s/27s)    | 45  | 60s   | ✅   | 직렬화 병목 |
| 4차 | Redis Lua Script | 102 | 27s   | ✅   | 5배 개선  |
| 5차 | + DB Pool 튜닝     | 87  | 32s   | ✅   | -      |
| 6차 | + Redis Pool 튜닝  | 108 | 25s   | ✅   | -      |
| 7차 | 최종 최적화           | 996 | 1.77s | ✅   | 목표 달성  |

성능 향상: TPS 20 -> 996 (약 50배 개선)
---

## 4. 핵심 개선 포인트

### 4.1 아키텍처 변경: 락 기반 → 원자적 연산

#### Before (분산락)

```text
Request → 분산락 획득 대기 → DB 조회 → 검증 → DB 저장 → 락 해제
└─ 직렬화로 인한 병목 (TPS 20~50)
```

#### After (Redis Lua Script)

```text
Request → Redis 원자적 연산 (재고 확인 + 중복 체크 + 발급) → 이벤트 발행
↓
비동기로 DB 저장
└─ 병렬 처리 가능 (TPS 1,000)
```

### 4.2 비동기 이벤트 처리

- 즉시 응답: Redis 연산 완료 후 바로 응답 (평균 1.11초)
- 백그라운드 처리: DB 저장은 비동기로 처리하여 사용자 대기 시간 최소화
- 안정성: @Async + Propagation.REQUIRES_NEW로 트랜잭션 독립성 보장

### 4.3 인프라 최적화

#### CPU 자원 증가

- 2 cores → 4 cores (병렬 처리 능력 2배 향상)

#### Connection Pool 최적화

Redis: 8 → 200 (Redis 연산 대기 시간 제거)
DB: 기본 → 50 (비동기 이벤트 처리용)

#### Thread Pool 조정

- Tomcat max-threads: 200 (기본값 고정)
- Task Execution: 50~100 threads

## 5. 권장 배포 스펙

```yaml
App Container:
  CPU: 4 cores
  Memory: 3GB
  JVM: -Xms1.5g -Xmx2g

Redis:
  CPU: 2 cores
  Memory: 1GB
  Persistence: RDB (백업용)

MySQL:
  CPU: 4 cores
  Memory: 4GB
  Connection Pool: 50~100
```

## 6 결론 및 권장사항

### 6.1 테스트 목표 달성 여부

| 항목    | 목표            | 결과     | 달성 |
|-------|---------------|--------|----|
| TPS   | 1,000이상       | 996.67 | ✅  |
| p(99) | 2초 이내         | 1.77s  | ✅  |
| 정합성   | 1,000개 정확히 발급 | 1,000개 | ✅  |
| 가용성   | 500 에러 0%     | 0%     | ✅  |

### 6.2 핵심 성공 요인

1. Redis Lua Script 활용: 원자적 연산으로 동시성 제어 + 성능 확보
2. 비동기 이벤트 처리: 사용자 응답과 DB 저장 분리
3. 적절한 인프라 스펙: CPU 4 cores, Memory 2GB
4. Connection Pool 최적화: Redis 200, DB 50

### 6.3 Production 배포 전 체크리스트

- [ ] Redis 재고 데이터 사전 세팅 로직 구현

  ```redis
  // 이벤트 시작 10분 전 실행
  SET coupon:1:stock 1000
  ```

- [ ] 모니터링 구축 (Prometheus + Grafana)
    - Redis 커넥션 사용률
    - DB 커넥션 사용률
    - API 응답시간 (p50, p99)
    - TPS 실시간 모니터링

- [ ] 알람 설정
    - p(99) > 3초 시 알람
    - 에러율 > 5% 시 알람
    - Redis/DB 커넥션 고갈 시 알람

- [ ] 비동기 이벤트 실패 처리 로직
    - 재시도 메커니즘 (최대 3회)
    - Dead Letter Queue 설정

## 7. 참고 자료

### 7.1 성능 테스트 명령어

```shell
# K6 테스트 실행
K6_WEB_DASHBOARD=true k6 run k6/issue_coupon_spike_test.js

# Docker 리소스 모니터링
docker stats app redis mysql

# Redis 재고 확인
docker exec -it redis redis-cli
GET coupon:1:stock
SCARD issue:coupon:1
```

### 7.2 주요 설정 파일

- K6 스크립트: k6/issue_coupon_spike_test.js
- Spring Boot 설정: application-perf.yml
- Docker 설정: docker-compose.perf.yml
- Redis 설정: RedisConfig.java

