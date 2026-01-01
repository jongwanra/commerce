package kr.hhplus.be.commerce.infrastructure.redis;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisKeyPatterns {

	private static final String COUPON_ISSUE_SET = "coupon:issue:%s";
	private static final String COUPON_STOCK = "coupon:stock:%s";

	public static String couponIssueKey(long couponId) {
		return String.format(COUPON_ISSUE_SET, couponId);
	}

	public static String couponStockKey(long couponId) {
		return String.format(COUPON_STOCK, couponId);
	}
}
