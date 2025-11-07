package kr.hhplus.be.commerce.infrastructure.global.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
	String key();

	TimeUnit timeUnit() default TimeUnit.SECONDS;

	/**
	 * 락을 기다리는 시간입니다.
	 * 락 획득을 위해 waitTime 만큼 대기합니다.
	 */
	long waitTime() default 5L;

	/**
	 * 락 임대 시간 입니다.
	 * 락을 획득한 이후에 leaseTime이 지나면 락을 해제합니다.
	 */
	long leaseTime() default 3L;
}
