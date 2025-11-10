package kr.hhplus.be.commerce.infrastructure.global.lock;

import java.lang.reflect.Method;
import java.util.List;
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

@Aspect
@Component
@RequiredArgsConstructor
// @Transactional 보다 먼저 실행할 수 있도록 순서를 설정합니다.
@Order(Ordered.LOWEST_PRECEDENCE - 1)
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

		final List<String> keys = Stream.of(ids)
			.map(id -> generateKey(id, distributedLock.key()))
			.peek(key -> log.info("key: {}", key))
			.toList();

		RLock[] rLocks = keys.stream()
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
			log.error("[DistributedLock] error occurred: key={}, method={}", keys, method, e);
			throw e;
		} finally {
			multiLock.unlock();
		}

	}

	private static String generateKey(String id, String key) {
		return REDISSON_LOCK_PREFIX
			+ KEY_DELIMITER
			+ key
			+ KEY_DELIMITER
			+ id;
	}
}
