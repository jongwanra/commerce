package kr.hhplus.be.commerce.application.coupon;

import static kr.hhplus.be.commerce.application.coupon.UserCouponIssueProcessor.*;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import net.bytebuddy.utility.RandomString;

import kr.hhplus.be.commerce.domain.coupon.model.Coupon;
import kr.hhplus.be.commerce.domain.coupon.model.enums.CouponDiscountType;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.global.AbstractIntegrationTestSupport;
import kr.hhplus.be.commerce.global.annotation.IntegrationTest;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.CouponJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.UserCouponJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.entity.CouponEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.entity.UserCouponEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.user.entity.UserEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.user.entity.enums.UserStatus;

class UserCouponIssueProcessorIntegrationTest extends AbstractIntegrationTestSupport {
	private static final Logger log = LoggerFactory.getLogger(UserCouponIssueProcessorIntegrationTest.class);
	@Autowired
	private UserCouponIssueProcessor userCouponIssueProcessor;

	@Autowired
	private CouponJpaRepository couponJpaRepository;

	@Autowired
	private UserCouponJpaRepository userCouponJpaRepository;

	/**
	 * 작성 이유: 사용자 100명이 동시에 총 10개의 수량을 가진 쿠폰에 대해 발급을 시도할 때,
	 * 선착순으로 총 10명의 사용자만 발급되었는지 검증하기 위해 작성했습니다.
	 */
	@IntegrationTest
	void 사용자_100명이_동시에_10개의_수량을_가진_쿠폰에_접근했을_때_10명의_사용자만_쿠폰을_발급하고_나머지는_예외를_발생시킨다() throws InterruptedException {
		// given
		final int userCount = 100;
		final int couponQuantity = 10;

		// 총 100명의 사용자를 생성합니다.
		List<UserEntity> users = IntStream.range(0, userCount)
			.mapToObj((index) -> UserEntity
				.builder()
				.email("user." + index + "@gmail.com")
				.encryptedPassword(RandomString.make(10))
				.status(UserStatus.ACTIVE)
				.build())
			.map((userEntity -> userJpaRepository.save(userEntity)))
			.toList();

		// 총 10개의 수량을 가진 쿠폰을 생성합니다.
		final LocalDateTime now = LocalDateTime.now();
		final LocalDateTime expiredAt = now.plusDays(7);

		CouponEntity coupon = couponJpaRepository.save(
			CouponEntity.fromDomain(Coupon.restore(
				null,
				"전상품 50% 할인",
				couponQuantity,
				expiredAt,
				CouponDiscountType.PERCENT,
				BigDecimal.valueOf(50)
			))
		);

		CountDownLatch countDownLatch = new CountDownLatch(userCount);
		ExecutorService executorService = Executors.newFixedThreadPool(userCount);
		// when
		IntStream.range(0, userCount).forEach(index -> executorService.execute(() -> {
			{
				UserEntity user = users.get(index);
				Command command = new Command(
					user.getId(), coupon.getId(), now);
				try {
					userCouponIssueProcessor.execute(command);
				} catch (CommerceException e) {
					log.info(e.getMessage());
				} finally {
					countDownLatch.countDown();
				}

			}
		}));

		countDownLatch.await();

		// then
		CouponEntity outOfStockCoupon = couponJpaRepository.findById(coupon.getId())
			.orElseThrow();
		assertThat(outOfStockCoupon.getStock()).isZero().as("쿠폰의 잔여 재고는 없습니다.");

		log.info("outOfStockCoupon = {}", outOfStockCoupon.getStock());
		List<UserCouponEntity> userCoupons = userCouponJpaRepository.findAll();
		assertThat(userCoupons.size()).isEqualTo(10).as("총 10명의 사용자만 쿠폰이 발급되었습니다.");
	}

}
