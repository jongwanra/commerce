package kr.hhplus.be.commerce.domain.coupon.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import kr.hhplus.be.commerce.domain.coupon.model.enums.CouponDiscountType;
import kr.hhplus.be.commerce.domain.global.annotation.InfrastructureOnly;
import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record Coupon(
	Long id,
	String name,
	Integer stock,
	LocalDateTime expiredAt,
	CouponDiscountType discountType,
	BigDecimal discountAmount
) {

	@InfrastructureOnly
	public static Coupon restore(Long id, String name, Integer stock, LocalDateTime expiredAt,
		CouponDiscountType discountType, BigDecimal discountAmount) {
		return Coupon.builder()
			.id(id)
			.name(name)
			.stock(stock)
			.expiredAt(expiredAt)
			.discountType(discountType)
			.discountAmount(discountAmount)
			.build();
	}

	public Coupon issue(LocalDateTime now) {
		validateIssuable(now);

		final int newStock = stock - 1;
		return Coupon.builder()
			.id(id)
			.name(name)
			.stock(newStock)
			.expiredAt(expiredAt)
			.discountType(discountType)
			.discountAmount(discountAmount)
			.build();
	}

	private void validateIssuable(LocalDateTime now) {
		validateExpired(now);
		validateStockIsRemaining();
	}

	private void validateStockIsRemaining() {
		if (this.stock <= 0) {
			throw new CommerceException(CommerceCode.OUT_OF_STOCK_COUPON);
		}
	}

	// 경계값 검증: 만료 시간과 현재 시간이 같은 경우도 만료된 것으로 간주 합니다.
	private void validateExpired(LocalDateTime now) {
		if (this.expiredAt.isBefore(now) || this.expiredAt.isEqual(now)) {
			throw new CommerceException(CommerceCode.EXPIRED_COUPON);
		}
	}
}
