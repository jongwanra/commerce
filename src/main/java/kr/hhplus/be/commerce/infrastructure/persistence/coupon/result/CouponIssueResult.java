package kr.hhplus.be.commerce.infrastructure.persistence.coupon.result;

import static java.util.Objects.*;

import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Getter
@RequiredArgsConstructor
@Slf4j
public enum CouponIssueResult {
	SOLD_OUT, DUPLICATE, SUCCESS, NOT_INITIALIZED;

	public static CouponIssueResult from(String text) {
		if (isNull(text) || text.isBlank()) {
			log.error("Redis Lua Script returned null or empty result");
			throw new CommerceException("쿠폰 발급 처리 중 오류가 발생했습니다");
		}
		try {
			return CouponIssueResult.valueOf(text.toUpperCase());
		} catch (IllegalArgumentException e) {
			log.error("Unexpected Redis result: {}", text, e);
			throw new CommerceException("쿠폰 발급 처리 중 오류가 발생했습니다");
		}
	}
}
