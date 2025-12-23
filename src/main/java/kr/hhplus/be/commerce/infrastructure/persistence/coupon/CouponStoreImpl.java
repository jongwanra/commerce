package kr.hhplus.be.commerce.infrastructure.persistence.coupon;

import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import kr.hhplus.be.commerce.domain.coupon.repository.CouponStore;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.result.CouponIssueResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CouponStoreImpl implements CouponStore {
	private static final String ISSUE_COUPON_KEY = "issue:coupon:%s";
	private static final String COUPON_STOCK_KEY = "coupon:%s:stock";
	private static final DefaultRedisScript<String> ISSUE_COUPON_SCRIPT = new DefaultRedisScript<>(
		getIssueCouponScript(),
		String.class);

	private final RedisTemplate<String, String> redisTemplate;

	@Override
	public CouponIssueResult issue(long couponId, long userId) {
		final String issueCouponKey = String.format(ISSUE_COUPON_KEY, couponId);
		final String stockCouponKey = String.format(COUPON_STOCK_KEY, couponId);

		return CouponIssueResult.from(redisTemplate.execute(
			ISSUE_COUPON_SCRIPT,
			List.of(issueCouponKey, stockCouponKey), // KEYS[1], KEYS[2]
			String.valueOf(userId) // ARGV[1]
		));

	}

	private static String getIssueCouponScript() {
		return "local limit = tonumber(redis.call('GET', KEYS[2])) "
			+ "local current_count = redis.call('SCARD', KEYS[1]) "
			+ "if current_count >= limit then return 'SOLD_OUT' end "
			+ "if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then return 'DUPLICATE' end "
			+ "redis.call('SADD', KEYS[1], ARGV[1]) "
			+ "return 'SUCCESS'";
	}
}
