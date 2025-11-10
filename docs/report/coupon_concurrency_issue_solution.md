# [동시성 이슈별 해결 전략] Coupon

## 목차

- [쿠폰 선착순 발급](#쿠폰-선착순-발급)
    - [1. 문제 상황](#1-문제-상황)
    - [2. 문제 원인 분석](#2-문제-원인-분석)
        - [2-1. 재고 차감(동시 읽기로 인한 검증 우회)](#2-1-재고-차감-동시-읽기로-인한-검증-우회)
        - [2-2. 재고 검증의 Race condition](#2-2-재고-검증의-race-condition)
        - [2-3. 동일 사용자의 중복 발급 가능성](#2-3-동일-사용자의-중복-발급-가능성)
        - [2-4. 결과](#2-4-결과)
    - [3. 해결 전략 및 적용](#3-해결-전략-및-적용)
        - [3-1. 해결 전략](#3-1-해결-전략)
        - [3-2. 적용](#3-2-적용)
    - [4. 적용 결과](#4-적용-결과)

## 쿠폰 선착순 발급

### 1. 문제 상황

10명 한정으로 전 상품을 50% 할인된 가격으로 구매할 수 있는 쿠폰 이벤트를 열었습니다.
이 때, 100명의 사용자가 쿠폰을 발급 받기 위해서 몰린 상황을 테스트 코드로 작성했습니다.

#### [Business Code] UserCouponIssueProcessor

```java

@Slf4j
@RequiredArgsConstructor
public class UserCouponIssueProcessor {
	private final CouponRepository couponRepository;
	private final UserCouponRepository userCouponRepository;

	@Transactional
	public Output execute(Command command) {
		Coupon issuedCoupon = couponRepository.findById(command.couponId)
			.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_COUPON))
			.issue(command.now);

		if (userCouponRepository.existsByUserIdAndCouponId(command.userId, command.couponId)) {
			throw new CommerceException(CommerceCode.ALREADY_ISSUED_COUPON);
		}

		return new Output(
			couponRepository.save(issuedCoupon),
			userCouponRepository.save(UserCoupon.of(command.userId, issuedCoupon, command.now))
		);
	}

	public record Command(
		Long userId,
		Long couponId,
		LocalDateTime now
	) {
	}

	public record Output(
		Coupon coupon,
		UserCoupon userCoupon
	) {
	}
}

```

#### [Test Code] UserCouponIssueProcessorIntegrationTest

```java

class UserCouponIssueProcessorIntegrationTest extends AbstractIntegrationTestSupport {
	// ...
	@IntegrationTest
	void 사용자_100명이_동시에_10개의_수량을_가진_쿠폰에_접근했을_때_10명의_사용자만_쿠폰을_발급하고_나머지는_예외를_발생시킨다() throws InterruptedException {
		// given
		final int userCount = 100;
		final int couponQuantity = 10;

		// 총 100명의 사용자를 생성합니다.
		List<UserEntity> users = IntStream.range(0, userCount)
			.mapToObj((index) -> UserEntity
				.builder()
				.email("user." + index + "@gmail.com")
				.encryptedPassword(RandomString.make(10))
				.status(UserStatus.ACTIVE)
				.build())
			.map((userEntity -> userJpaRepository.save(userEntity)))
			.toList();

		// 총 10개의 수량을 가진 쿠폰을 생성합니다.
		final LocalDateTime now = LocalDateTime.now();
		final LocalDateTime expiredAt = now.plusDays(7);

		CouponEntity coupon = couponJpaRepository.save(
			CouponEntity.fromDomain(Coupon.restore(
				null,
				"전상품 50% 할인",
				couponQuantity,
				expiredAt,
				CouponDiscountType.PERCENT,
				BigDecimal.valueOf(50)
			))
		);

		CountDownLatch countDownLatch = new CountDownLatch(userCount);
		ExecutorService executorService = Executors.newFixedThreadPool(userCount);
		// when
		IntStream.range(0, userCount).forEach(index -> executorService.execute(() -> {
			{
				UserEntity user = users.get(index);
				Command command = new Command(user.getId(), coupon.getId(), now);
				try {
					userCouponIssueProcessor.execute(command);
				} catch (CommerceException e) {
					log.info(e.getMessage());
				} finally {
					countDownLatch.countDown();
				}

			}
		}));

		countDownLatch.await();

		// then
		CouponEntity outOfStockCoupon = couponJpaRepository.findById(coupon.getId())
			.orElseThrow();
		assertThat(outOfStockCoupon.getStock()).isZero().as("쿠폰의 잔여 재고는 없습니다.");

		List<UserCouponEntity> userCoupons = userCouponJpaRepository.findAll();
		assertThat(userCoupons.size()).isEqualTo(10).as("총 10명의 사용자만 쿠폰이 발급되었습니다.");
	}
}
```

#### 테스트 결과

| 회차 | 예상 결과             | 실제 결과             |
|:--:|-------------------|-------------------|
| 1회 | 재고: 0<br>발급 수: 10 | 재고: 0<br>발급 수: 30 |
| 2회 | 재고: 0<br>발급 수: 10 | 재고: 0<br>발급 수: 30 |
| 3회 | 재고: 0<br>발급 수: 10 | 재고: 0<br>발급 수: 30 |

예상했던 결과와 다르게 쿠폰의 발급 수가 10개가 아닌, 30개가 발급되었습니다.

## 2. 문제 원인 분석

재고는 정확히 0이 되었지만, 발급 수가 30개 된 이유는 아래와 같습니다.

### 2-1. 재고 차감 (동시 읽기로 인한 검증 우회)

실제 로그를 확인하면 여러 스레드가 동일한 재고값을 읽고 있습니다.

```text
thread-2: stock = 9
thread-1: stock = 9 // 동일한 값을 읽음
thread-4: stock = 9 // 동일한 값을 읽음

thread-3: stock = 8
thread-6: stock = 8 // 동일한 값을 읽음
thread-5: stock = 8 // 동일한 값을 읽음
```

- Coupon이 불변 객체이므로 `issue()` 호출 시 새로운 객체가 생성됩니다.
- 여러 스레드가 동시에 같은 쿠폰 재고값을 읽어 각각 차감된 객체를 생성합니다.
- 이후에 `couponRepository.save()` 시점에 Mysql InnoDB의 UPDATE 쿼리가 row-level lock을 자동으로 획득합니다.
- UPDATE는 순차적으로 처리되어 최종 재고는 0이 되지만, **이미 재고 검증을 통과한 모든 요청은 쿠폰이 발급됩니다.**
    - (100명의 사용자로 테스트를 진행했기 때문에 재고가 0으로 표기되었지만 100명의 이하의 사용자일 경우에는 재고가 남을 수 있습니다.)

### 2-2. 재고 검증의 Race condition

- 여러 스레드가 동시에 같은 재고값을 읽어 `issue()` 메서드의 재고 검증을 통과합니다.
- 예를 들어 3개의 스레드가 동시에 stock=9를 읽으면 모두 "재고가 존재한다"로 판단하게 됩니다.
- 각 스레드는 정상적으로 재고 차감 로직을 수행하고 `UserCoupon`을 저장하게 됩니다.

### 2-3. 동일 사용자의 중복 발급 가능성

- 현재 테스트는 **서로 다른 사용자 100명**이 1개의 쿠폰을 발급하는 상황을 가정했습니다.
- 하지만 실제 운영 환경에서는 동일 사용자가 여러 번 요청할 수 있으므로,
  중복 발급 방지를 위한 유니크 제약 조건이 필요합니다.

### 2-4. 결과

- 여러 스레드가 동시에 동일한 재고를 읽어 검증 로직을 통과했습니다.
- UPDATE 시 row-level lock과 coupon이 불변 객체이기 때문에 재고는 순차적으로 차감되지만, 이미 검증을 통과한 요청들은 모두 발급되었습니다.
- 결과적으로 예상(10명)보다 많은 사용자(30명)에게 쿠폰이 발급되었습니다.

## 3. 해결 전략 및 적용

### 3-1. 해결 전략

|       잠금 방식        | 채택 여부 | 이유                                                                                                    |
|:------------------:|-------|-------------------------------------------------------------------------------------------------------|
|       낙관적 락        | ❌     | 선착순 쿠폰 발급은 충돌이 빈번하게 발생합니다. 경합이 심할수록 성능 저하가 발생하므로 부적합합니다.                                              |
| 비관적 락 (FOR UPDATE) | ✅     | 충돌 발생 가능성이 높고 데이터 정합성이 중요한 상황에 적합합니다. PESSIMISTIC_WRITE를 사용하여 재고 조회 시 row-level lock을 획득해 동시성을 제어합니다. 
|       네임드 락        | ❌     | 비관적 락으로 충분히 해결 가능합니다. 네임드 락은 별도 커넥션 관리와 타임아웃 처리가 필요하기 때문에 복잡도가 높아집니다.                                 |
|     유니크 제약 조건      | ✅     | `(user_id, couponId)`에 유니크 제약 조건을 설정하여 DB 레벨에서 중복 발급을 방지합니다.                                          |

### 3-2. 적용

#### 1. 비관적 락 (FOR UPDATE)

```java

public interface CouponJpaRepository extends JpaRepository<CouponEntity, Long> {
	// ...

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT c FROM coupon c WHERE c.id = :couponId")
	Optional<CouponEntity> findByIdForUpdate(@Param("couponId") Long couponId);

	// ...
}
```

````java
public class UserCouponIssueProcessor {
	@Transactional
	public Output execute(Command command) {
		// 특정 coupon에 비관적 락을 획득하여, 동시성 제어를 합니다.
		Coupon issuedCoupon = couponRepository.findByIdForUpdate(command.couponId)
			.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_COUPON))
			.issue(command.now);

		// 사용자 경험을 위해 중복 체크를 애플리케이션 수준에서 사전 진행합니다. 
		if (userCouponRepository.existsByUserIdAndCouponId(command.userId, command.couponId)) {
			throw new CommerceException(CommerceCode.ALREADY_ISSUED_COUPON);
		}

		try {
			return new Output(
				couponRepository.save(issuedCoupon),
				userCouponRepository.save(UserCoupon.of(command.userId, issuedCoupon, command.now))
			);
		} catch (DataIntegrityViolationException e) {
			// 유니크 제약 조건 위반 할 경우의 예외 처리입니다.
			throw new CommerceException(CommerceCode.ALREADY_ISSUED_COUPON);
		}
	}
}
````

#### 2. 유니크 제약 조건

```sql
create table hhplus.user_coupon
(
    id        bigint auto_increment comment '고유 식별자' primary key,
    user_id   bigint not null comment '사용자 고유 식별자',
    coupon_id bigint not null comment '쿠폰 고유 식별자',
    -- ...
    -- 유니크 제약 조건 추가
    constraint uidx_user_coupon_user_id_coupon_id
        unique (user_id, coupon_id)
);


```

## 4. 적용 결과

| 회차 | 예상 결과             | 실제 결과             |
|:--:|-------------------|-------------------|
| 1회 | 재고: 0<br>발급 수: 10 | 재고: 0<br>발급 수: 10 |
| 2회 | 재고: 0<br>발급 수: 10 | 재고: 0<br>발급 수: 10 |
| 3회 | 재고: 0<br>발급 수: 10 | 재고: 0<br>발급 수: 10 |
