package kr.hhplus.be.commerce.domain.coupon.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserCouponStatus {
	AVAILABLE("사용 가능함"),
	USED("사용됨");

	private final String description;

	public boolean isUsed() {
		return this.equals(USED);
	}
}
