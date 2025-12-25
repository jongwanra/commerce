package kr.hhplus.be.commerce.domain.coupon.repository;

import kr.hhplus.be.commerce.infrastructure.persistence.coupon.result.CouponIssueResult;

public interface CouponStore {
	CouponIssueResult issue(long couponId, long userId);
}
