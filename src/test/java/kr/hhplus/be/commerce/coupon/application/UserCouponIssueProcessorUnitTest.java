package kr.hhplus.be.commerce.coupon.application;

import static kr.hhplus.be.commerce.coupon.application.UserCouponIssueProcessor.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.hhplus.be.commerce.AbstractUnitTestSupport;
import kr.hhplus.be.commerce.coupon.persistence.CouponEntity;
import kr.hhplus.be.commerce.coupon.persistence.CouponJpaRepository;
import kr.hhplus.be.commerce.coupon.persistence.UserCouponEntity;
import kr.hhplus.be.commerce.coupon.persistence.UserCouponRepository;
import kr.hhplus.be.commerce.coupon.persistence.enums.CouponDiscountType;
import kr.hhplus.be.commerce.global.exception.CommerceCode;
import kr.hhplus.be.commerce.global.exception.CommerceException;

@ExtendWith(MockitoExtension.class)
class UserCouponIssueProcessorUnitTest extends AbstractUnitTestSupport {
	@InjectMocks
	private UserCouponIssueProcessor userCouponIssueProcessor;
	@Mock
	private CouponJpaRepository couponJpaRepository;
	@Mock
	private UserCouponRepository userCouponRepository;

	// 작성 이유: 발급한 쿠폰이 없는 경우 예외를 발생시키는지 확인하기 위해 작성했습니다.
	@Test
	void 발급할_쿠폰이_존재하지_않는_경우_예외를_발생시킨다() {
		// given
		Long userId = 1222L;
		Long couponId = 1232L;
		LocalDateTime now = LocalDateTime.now();
		Command command = new Command(userId, couponId, now);

		// mock
		given(couponJpaRepository.findByIdWithLock(couponId))
			.willThrow(new CommerceException(CommerceCode.NOT_FOUND_COUPON));

		// when & then
		assertThatThrownBy(() -> {
			userCouponIssueProcessor.execute(command);
		})
			.isInstanceOf(CommerceException.class)
			.hasMessage("존재하지 않는 쿠폰입니다.");
		// then
	}

	// 작성 이유: 발급할 쿠폰이 만료된 쿠폰인 경우 예외를 발생시키는지 확인하기 위해 작성했습니다.[경계값 검증]
	// 만료된 쿠폰: 만료 시간이 현재 시간과 같거나 이전인 쿠폰을 의미합니다.
	@Test
	void 발급할_쿠폰이_만료된_쿠폰인_경우_예외를_발생시킨다() {
		// given
		Long userId = 1222L;
		Long couponId = 1232L;
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime expiredAt = now;
		Command command = new Command(userId, couponId, now);

		CouponEntity expiredCoupon = CouponEntity.builder()
			.discountAmount(BigDecimal.valueOf(5_000))
			.discountType(CouponDiscountType.FIXED)
			.stock(10)
			.expiredAt(expiredAt)
			.build();
		assignId(couponId, expiredCoupon);

		// mock
		given(couponJpaRepository.findByIdWithLock(couponId))
			.willReturn(Optional.of(expiredCoupon));

		// when & then
		assertThatThrownBy(() -> {
			userCouponIssueProcessor.execute(command);
		})
			.isInstanceOf(CommerceException.class)
			.hasMessage("만료된 쿠폰입니다.");

	}

	// 작성 이유: 발급할 쿠폰의 재고가 없는 경우 예외를 발생시키는지 확인하기 위해 작성했습니다.
	@Test
	void 발급할_쿠폰의_재고가_없는_경우_예외를_발생시킨다() {
		// given
		Long userId = 1222L;
		Long couponId = 1232L;
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime expiredAt = now.plusSeconds(1L);
		Command command = new Command(userId, couponId, now);

		CouponEntity outOfStockCoupon = CouponEntity.builder()
			.discountAmount(BigDecimal.valueOf(5_000))
			.discountType(CouponDiscountType.FIXED)
			.stock(0)
			.expiredAt(expiredAt)
			.build();

		assignId(couponId, outOfStockCoupon);

		// mock
		given(couponJpaRepository.findByIdWithLock(couponId))
			.willReturn(Optional.of(outOfStockCoupon));

		// when & then
		assertThatThrownBy(() -> {
			userCouponIssueProcessor.execute(command);
		})
			.isInstanceOf(CommerceException.class)
			.hasMessage("재고가 소진된 쿠폰입니다.");
	}

	// 작성 이유: 이미 발급받은 쿠폰인 경우 예외를 발생시키는지 확인하기 위해 작성했습니다.
	@Test
	void 이미_발급받은_쿠폰인_경우_예외를_발생시킨다() {
		// given
		Long userId = 1222L;
		Long couponId = 1232L;
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime expiredAt = now.plusSeconds(1L);
		Command command = new Command(userId, couponId, now);

		CouponEntity alreadyIssuedCoupon = CouponEntity.builder()
			.discountAmount(BigDecimal.valueOf(5_000))
			.discountType(CouponDiscountType.FIXED)
			.stock(10)
			.expiredAt(expiredAt)
			.build();
		assignId(couponId, alreadyIssuedCoupon);

		UserCouponEntity userCoupon = UserCouponEntity.of(userId, alreadyIssuedCoupon, now);

		// mock
		given(couponJpaRepository.findByIdWithLock(couponId))
			.willReturn(Optional.of(alreadyIssuedCoupon));

		given(userCouponRepository.existsByUserIdAndCouponId(userId, couponId))
			.willReturn(true);

		// when & then
		assertThatThrownBy(() -> {
			userCouponIssueProcessor.execute(command);
		})
			.isInstanceOf(CommerceException.class)
			.hasMessage("이미 발급 받은 쿠폰입니다.");
	}

	// 작성 이유: 쿠폰을 성공적으로 발급하는지 확인하기 위해 작성했습니다.
	@Test
	void 쿠폰을_성공적으로_발급한다() {
		// given
		Long userId = 1222L;
		Long couponId = 1232L;
		Long userCouponId = 4321L;
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime expiredAt = now.plusSeconds(1L);
		Command command = new Command(userId, couponId, now);

		CouponEntity coupon = CouponEntity.builder()
			.discountAmount(BigDecimal.valueOf(5_000))
			.discountType(CouponDiscountType.FIXED)
			.stock(10)
			.expiredAt(expiredAt)
			.build();
		assignId(couponId, coupon);

		UserCouponEntity userCoupon = UserCouponEntity.of(userId, coupon, now);

		UserCouponEntity assignedUserCoupon = UserCouponEntity.of(userId, coupon, now);
		assignId(userCouponId, assignedUserCoupon);

		// mock
		given(couponJpaRepository.findByIdWithLock(couponId))
			.willReturn(Optional.of(coupon));

		given(userCouponRepository.save(userCoupon))
			.willReturn(assignedUserCoupon);

		// when
		Output output = userCouponIssueProcessor.execute(command);

		// then
		UserCouponEntity userCouponOutput = output.userCoupon();
		assertThat(userCouponOutput.getId()).isEqualTo(userCouponId);
		assertThat(userCouponOutput.getUserId()).isEqualTo(userId);
		assertThat(userCouponOutput.getCouponId()).isEqualTo(couponId);
		assertThat(userCouponOutput.getIssuedAt()).isEqualTo(now);

		CouponEntity couponOutput = output.coupon();
		assertThat(couponOutput.getId()).isEqualTo(couponId);
		assertThat(couponOutput.getDiscountAmount()).isEqualTo(BigDecimal.valueOf(5_000));
		assertThat(couponOutput.getDiscountType()).isEqualTo(CouponDiscountType.FIXED);
		assertThat(couponOutput.getStock()).isEqualTo(9).as("쿠폰 재고가 1 감소했는지 확인합니다.");
		assertThat(couponOutput.getExpiredAt()).isEqualTo(expiredAt);

	}

}
