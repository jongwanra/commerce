package kr.hhplus.be.commerce.infrastructure.persistence.coupon.entity;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.entity.enums.CouponDiscountType;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.entity.enums.UserCouponStatus;

class UserCouponEntityTest {

	@Test
	void 본인_소유가_아닌_쿠폰을_사용할_경우_예외를_발생시킨다() {
		// given
		Long userIdHavingCoupon = 123L;
		Long userIdToUseCoupon = 321L;
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime issuedAt = now.minusDays(3);
		LocalDateTime expiredAt = issuedAt.plusDays(7); // 쿠폰을 발급한 일시로 부터 1주일간 유효한 쿠폰입니다.

		CouponEntity coupon = CouponEntity.builder()
			.stock(10)
			.discountType(CouponDiscountType.FIXED)
			.discountAmount(BigDecimal.valueOf(10_000))
			.name("전 상품 10,000원 할인 쿠폰")
			.expiredAt(expiredAt)
			.build();

		UserCouponEntity userCoupon = UserCouponEntity.of(userIdHavingCoupon, coupon, issuedAt);

		// when & then
		assertThatThrownBy(() -> userCoupon.use(userIdToUseCoupon, now, 1L))
			.isInstanceOf(CommerceException.class)
			.hasMessage("접근 권한이 없는 사용자입니다.");

	}

	@Test
	void 일초_전에_만료된_쿠폰을_사용할_경우_예외를_발생시킨다() {
		// given
		Long userId = 123L;
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime expiredAt = now.minusSeconds(1);
		LocalDateTime issuedAt = now.minusDays(3);

		CouponEntity coupon = CouponEntity.builder()
			.stock(10)
			.discountType(CouponDiscountType.FIXED)
			.discountAmount(BigDecimal.valueOf(10_000))
			.name("전 상품 10,000원 할인 쿠폰")
			.expiredAt(expiredAt)
			.build();

		UserCouponEntity userCoupon = UserCouponEntity.of(userId, coupon, issuedAt);

		// when & then
		assertThatThrownBy(() -> userCoupon.use(userId, now, 1L))
			.isInstanceOf(CommerceException.class)
			.hasMessage("만료된 쿠폰입니다.");

	}

	@Test
	void 쿠폰_사용_시간과_만료_시간이_정확히_일치할_경우_예외를_발생시킨다() {
		// given
		Long userId = 123L;
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime expiredAt = now;
		LocalDateTime issuedAt = now.minusDays(3);

		CouponEntity coupon = CouponEntity.builder()
			.stock(10)
			.discountType(CouponDiscountType.FIXED)
			.discountAmount(BigDecimal.valueOf(10_000))
			.name("전 상품 10,000원 할인 쿠폰")
			.expiredAt(expiredAt)
			.build();

		UserCouponEntity userCoupon = UserCouponEntity.of(userId, coupon, issuedAt);

		// when & then
		assertThatThrownBy(() -> userCoupon.use(userId, now, 1L))
			.isInstanceOf(CommerceException.class)
			.hasMessage("만료된 쿠폰입니다.");
	}

	@Test
	void 쿠폰을_정상적으로_사용할_수_있다() {
		// given
		Long userId = 123L;
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime issuedAt = now.minusDays(3);
		LocalDateTime expiredAt = issuedAt.plusDays(7);

		CouponEntity coupon = CouponEntity.builder()
			.stock(10)
			.discountType(CouponDiscountType.FIXED)
			.discountAmount(BigDecimal.valueOf(10_000))
			.name("전 상품 10,000원 할인 쿠폰")
			.expiredAt(expiredAt)
			.build();

		UserCouponEntity userCoupon = UserCouponEntity.of(userId, coupon, issuedAt);
		// when
		userCoupon.use(userId, now, 1L);

		// then
		assertThat(userCoupon.getStatus()).isEqualTo(UserCouponStatus.USED);
		assertThat(userCoupon.getLastUsedAt()).isEqualTo(now);
		assertThat(userCoupon.getOrderId()).isEqualTo(1L);
	}

	@Test
	void 백분율_할인_쿠폰을_적용할_수_있다() {
		// given
		Long userId = 123L;
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime issuedAt = now.minusDays(3);
		LocalDateTime expiredAt = issuedAt.plusDays(7);

		CouponEntity coupon = CouponEntity.builder()
			.stock(10)
			.discountType(CouponDiscountType.PERCENT)
			.discountAmount(BigDecimal.valueOf(10))
			.name("상품 10% 할인 쿠폰")
			.expiredAt(expiredAt)
			.build();

		UserCouponEntity userCoupon = UserCouponEntity.of(userId, coupon, issuedAt);

		// when & then
		assertThat(userCoupon.calculateFinalAmount(BigDecimal.valueOf(10_000))).isEqualByComparingTo(
			BigDecimal.valueOf(9_000));
		assertThat(userCoupon.calculateFinalAmount(BigDecimal.valueOf(50_000))).isEqualByComparingTo(
			BigDecimal.valueOf(45_000));
		assertThat(userCoupon.calculateFinalAmount(BigDecimal.valueOf(123_450))).isEqualByComparingTo(
			BigDecimal.valueOf(111_105));
	}

	@Test
	void 이미_사용한_쿠폰일_경우_예외를_발생시킨다() {
		// given
		Long userId = 123L;
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime issuedAt = now.minusDays(3);
		LocalDateTime expiredAt = issuedAt.plusDays(7);

		CouponEntity coupon = CouponEntity.builder()
			.stock(10)
			.discountType(CouponDiscountType.FIXED)
			.discountAmount(BigDecimal.valueOf(10_000))
			.name("전 상품 10,000원 할인 쿠폰")
			.expiredAt(expiredAt)
			.build();

		UserCouponEntity userCoupon = UserCouponEntity.of(userId, coupon, issuedAt);
		userCoupon.use(userId, now, 1L);

		assertThat(userCoupon.getStatus()).isEqualTo(UserCouponStatus.USED);

		// when & then
		assertThatThrownBy(() -> userCoupon.use(userId, now, 1L))
			.isInstanceOf(CommerceException.class)
			.hasMessage("이용 불가능한 쿠폰입니다.");

	}

}
