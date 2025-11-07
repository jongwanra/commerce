package kr.hhplus.be.commerce.infrastructure.global.lock;

import java.lang.reflect.Method;

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
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RedissonLockAspect {
	private static final String REDISSON_LOCK_PREFIX = "lock:";
	private final RedissonClient redissonClient;

	@Around("@annotation(kr.hhplus.be.commerce.infrastructure.global.lock.DistributedLock)")
	public Object lock(ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
		Method method = methodSignature.getMethod();
		DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);
		final String key =
			REDISSON_LOCK_PREFIX + CommerceSpringELParser.parse(methodSignature.getParameterNames(),
				joinPoint.getArgs(), distributedLock.key());

		log.info("key = {}", key);

		RLock rLock = redissonClient.getLock(key);

		try {
			final boolean isAvailable = rLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(),
				distributedLock.timeUnit());
			if (!isAvailable) {
				return false;
			}
			return joinPoint.proceed();
		} catch (InterruptedException e) {
			log.error("InterruptedException occurred. error message={}", e.getMessage(), e);
			throw new CommerceException(CommerceCode.EXCEEDED_WAIT_TIME_FOR_DISTRIBUTED_LOCK);
		} finally {
			rLock.unlock();
		}

	}
}
