package kr.hhplus.be.commerce.infrastructure.redis;

import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis를 헬스체크하는 스케줄러입니다.
 * 30초 마다 주기적으로 시스템이 정상 동작하는지 체크합니다.
 * Redis 시스템 장애 발생시, log.error를 통해 개발자에게 알림을 보냅니다.
 * TODO 외부 로깅 시스템 연동이 필요합니다.
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisHealthCheckScheduler {
	private static final int THIRTY_SECONDS = 1000 * 30;
	private final RedisTemplate<String, String> redisTemplate;

	@Scheduled(fixedDelay = THIRTY_SECONDS)
	public void execute() {
		try {
			redisTemplate.opsForValue().get("health-check");
		} catch (RedisConnectionFailureException e) {
			log.error("❌ Redis에 장애가 발생했습니다. 서비스는 DB fallback으로 동작 중입니다.", e);
		} catch (Exception e) {
			log.error("❌ Redis Health Check 실행 중 예상치 못한 오류가 발생했습니다.", e);
		}
	}
}
