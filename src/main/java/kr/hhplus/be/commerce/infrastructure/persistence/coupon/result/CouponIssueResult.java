package kr.hhplus.be.commerce.infrastructure.persistence.coupon.result;

import static java.util.Objects.*;

import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CouponIssueResult {
	SOLD_OUT, DUPLICATE, SUCCESS;

	public static CouponIssueResult from(String text) {
		if (isNull(text) || text.isBlank()) {
			throw new CommerceException("CouponIssueResult text cannot be null or empty");
		}
		return CouponIssueResult.valueOf(text.toUpperCase());
	}
}
