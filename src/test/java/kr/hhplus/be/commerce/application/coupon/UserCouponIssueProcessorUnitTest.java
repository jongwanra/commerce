package kr.hhplus.be.commerce.application.coupon;

import static kr.hhplus.be.commerce.application.coupon.UserCouponIssueProcessor.*;
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

import kr.hhplus.be.commerce.domain.coupon.model.Coupon;
import kr.hhplus.be.commerce.domain.coupon.model.UserCoupon;
import kr.hhplus.be.commerce.domain.coupon.model.enums.CouponDiscountType;
import kr.hhplus.be.commerce.domain.coupon.model.enums.UserCouponStatus;
import kr.hhplus.be.commerce.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.commerce.domain.coupon.repository.UserCouponRepository;
import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.global.AbstractUnitTestSupport;

@ExtendWith(MockitoExtension.class)
class UserCouponIssueProcessorUnitTest extends AbstractUnitTestSupport {
	@InjectMocks
	private UserCouponIssueProcessor userCouponIssueProcessor;
	@Mock
	private CouponRepository couponRepository;
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
		given(couponRepository.findByIdForUpdate(couponId))
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

		Coupon expiredCoupon = Coupon.restore(
			couponId,
			"-",
			10,
			expiredAt,
			CouponDiscountType.FIXED,
			BigDecimal.valueOf(5_000)
		);

		// mock
		given(couponRepository.findByIdForUpdate(couponId))
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

		Coupon outOfStockCoupon = Coupon.restore(couponId, "-", 0, expiredAt, CouponDiscountType.FIXED,
			BigDecimal.valueOf(5_000));

		// mock
		given(couponRepository.findByIdForUpdate(couponId))
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

		Coupon alreadyIssuedCoupon = Coupon.restore(couponId, "-", 10, expiredAt, CouponDiscountType.FIXED,
			BigDecimal.valueOf(5_000));

		// mock
		given(couponRepository.findByIdForUpdate(couponId))
			.willReturn(Optional.of(alreadyIssuedCoupon));

		given(userCouponRepository.existsByUserIdAndCouponId(userId, couponId))
			.willReturn(true);

		// when & then
		assertThatThrownBy(() -> userCouponIssueProcessor.execute(command))
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

		Coupon coupon = Coupon.restore(couponId, "-", 10, expiredAt, CouponDiscountType.FIXED,
			BigDecimal.valueOf(5_000));
		Coupon issuedCoupon = coupon.issue(now);

		UserCoupon userCoupon = UserCoupon.restore(
			userCouponId,
			userId,
			couponId,
			null,
			coupon.name(),
			coupon.discountType(),
			coupon.discountAmount(),
			UserCouponStatus.AVAILABLE,
			now,
			coupon.expiredAt(),
			null,
			null
		);

		// mock
		given(couponRepository.findByIdForUpdate(couponId))
			.willReturn(Optional.of(coupon));

		given(userCouponRepository.save(any(UserCoupon.class)))
			.willReturn(userCoupon);

		given(couponRepository.save(any(Coupon.class)))
			.willReturn(issuedCoupon);

		// when
		Output output = userCouponIssueProcessor.execute(command);

		// then
		UserCoupon userCouponOutput = output.userCoupon();
		assertThat(userCouponOutput.id()).isEqualTo(userCouponId);
		assertThat(userCouponOutput.userId()).isEqualTo(userId);
		assertThat(userCouponOutput.couponId()).isEqualTo(couponId);
		assertThat(userCouponOutput.issuedAt()).isEqualTo(now);

		Coupon couponOutput = output.coupon();
		assertThat(couponOutput.id()).isEqualTo(couponId);
		assertThat(couponOutput.discountAmount()).isEqualTo(BigDecimal.valueOf(5_000));
		assertThat(couponOutput.discountType()).isEqualTo(CouponDiscountType.FIXED);
		assertThat(couponOutput.stock()).isEqualTo(9).as("쿠폰 재고가 1 감소했는지 확인합니다.");
		assertThat(couponOutput.expiredAt()).isEqualTo(expiredAt);

	}

}
