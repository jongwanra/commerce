package kr.hhplus.be.commerce.infrastructure.persistence.coupon;

import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import kr.hhplus.be.commerce.domain.coupon.repository.CouponStore;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.result.CouponIssueResult;
import kr.hhplus.be.commerce.infrastructure.redis.RedisKeyPatterns;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CouponStoreImpl implements CouponStore {
	private static final DefaultRedisScript<String> ISSUE_COUPON_SCRIPT = new DefaultRedisScript<>(
		getIssueCouponScript(),
		String.class);

	private final RedisTemplate<String, String> redisTemplate;

	@Override
	public CouponIssueResult issue(long couponId, long userId) {

		return CouponIssueResult.from(redisTemplate.execute(
			ISSUE_COUPON_SCRIPT,
			List.of(RedisKeyPatterns.couponIssueKey(couponId), RedisKeyPatterns.couponStockKey(couponId)),
			// KEYS[1], KEYS[2]
			String.valueOf(userId) // ARGV[1]
		));

	}

	private static String getIssueCouponScript() {
		return "local limit = tonumber(redis.call('GET', KEYS[2])) "
			+ "if not limit then return 'NOT_INITIALIZED' end "
			+ "local current_count = redis.call('SCARD', KEYS[1]) "
			+ "if current_count >= limit then return 'SOLD_OUT' end "
			+ "if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then return 'DUPLICATE' end "
			+ "redis.call('SADD', KEYS[1], ARGV[1]) "
			+ "return 'SUCCESS'";
	}
}
