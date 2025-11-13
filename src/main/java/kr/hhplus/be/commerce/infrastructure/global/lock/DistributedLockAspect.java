package kr.hhplus.be.commerce.infrastructure.global.lock;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * {@link DistributedLock}이 붙은 메서드에 대해 분산락을 적용합니다.
 *
 * <p>Example: {@link kr.hhplus.be.commerce.application.coupon.UserCouponIssueWithDistributedLockProcessor}</p>
 */
@Aspect
@Component
@RequiredArgsConstructor
// @Transactional 보다 먼저 실행할 수 있도록 순서를 설정합니다.
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class DistributedLockAspect {
	private static final String REDISSON_LOCK_PREFIX = "lock";
	private static final String KEY_DELIMITER = ":";
	private static final String ID_DELIMITER = ",";

	private final RedissonClient redissonClient;

	@Around("@annotation(DistributedLock)")
	public Object lock(ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
		Method method = methodSignature.getMethod();

		DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);
		String[] ids = CommerceSpringELParser.parse(methodSignature.getParameterNames(), joinPoint.getArgs(),
			distributedLock.keyExpression()).split(ID_DELIMITER);

		final List<String> lockKeys = Stream.of(ids)
			.map(id -> generateLockKey(id, distributedLock.key()))
			.toList();

		RLock[] rLocks = lockKeys.stream()
			.map(redissonClient::getLock)
			.toArray(RLock[]::new);

		RLock multiLock = redissonClient.getMultiLock(rLocks);

		try {
			final boolean isAcquired = multiLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(),
				distributedLock.timeUnit());

			if (!isAcquired) {
				throw new CommerceException(CommerceCode.FAILED_FOR_ACQUIRING_DISTRIBUTED_LOCK);
			}
			return joinPoint.proceed();
		} catch (Exception e) {
			log.error("[DistributedLock] error occurred: key={}, method={}", lockKeys, method, e);
			throw e;
		} finally {
			unlock(multiLock, lockKeys);

		}

	}

	/**
	 * 잠금을 획득하지 못한 상황해서 unlock을 수행할 경우 {@link CompletionException}이 발생합니다.
	 *
	 * <p>{@link org.redisson.RedissonBaseLock}:330을 참고해주세요.</p>
	 * <ol>
	 * 	<li>leaseTime이 지나 자동으로 잠금 해제됨</li>
	 * 	<li>잠금을 획득하지 못한 채 waitTime이 지남</li>
	 * </ol>
	 */
	private void unlock(RLock multiLock, List<String> lockKeys) {
		try {
			multiLock.unlock();
		} catch (CompletionException e) {
			log.warn("[DistributedLock] lock already released: key={}", lockKeys);
		}

	}

	private String generateLockKey(String id, String key) {
		return REDISSON_LOCK_PREFIX
			+ KEY_DELIMITER
			+ key
			+ KEY_DELIMITER
			+ id;
	}
}
