package kr.hhplus.be.commerce.payment.application;

import static kr.hhplus.be.commerce.payment.application.PaymentMakeProcessor.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.hhplus.be.commerce.cash.persistence.CashEntity;
import kr.hhplus.be.commerce.cash.persistence.CashHistoryRepository;
import kr.hhplus.be.commerce.cash.persistence.CashRepository;
import kr.hhplus.be.commerce.coupon.persistence.CouponEntity;
import kr.hhplus.be.commerce.coupon.persistence.UserCouponEntity;
import kr.hhplus.be.commerce.coupon.persistence.UserCouponRepository;
import kr.hhplus.be.commerce.coupon.persistence.enums.CouponDiscountType;
import kr.hhplus.be.commerce.coupon.persistence.enums.UserCouponStatus;
import kr.hhplus.be.commerce.global.exception.CommerceException;
import kr.hhplus.be.commerce.order.domain.model.Order;
import kr.hhplus.be.commerce.order.domain.model.OrderLine;
import kr.hhplus.be.commerce.order.domain.model.enums.OrderStatus;
import kr.hhplus.be.commerce.order.domain.repository.OrderRepository;
import kr.hhplus.be.commerce.payment.domain.model.Payment;
import kr.hhplus.be.commerce.payment.domain.model.enums.PaymentStatus;
import kr.hhplus.be.commerce.payment.domain.model.enums.PaymentTargetType;
import kr.hhplus.be.commerce.payment.domain.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentMakeProcessorUnitTest {
	@InjectMocks
	private PaymentMakeProcessor paymentMakeProcessor;
	@Mock
	private PaymentRepository paymentRepository;
	@Mock
	private OrderRepository orderRepository;
	@Mock
	private UserCouponRepository userCouponRepository;
	@Mock
	private CashRepository cashRepository;

	@Mock
	private CashHistoryRepository cashHistoryRepository;

	// 작성 이유: 주문한 사용자만 결제가 가능한지 검증하기 위해서 작성했습니다.
	@Test
	void 주문한_사용자와_결제_사용자가_다를_경우_예외를_발생시킨다() {
		// given
		final Long orderId = 122L;
		final Long ordererUserId = 1L;
		final Long payerUserId = 2L;
		final Long orderLineId = 15L;

		OrderLine orderLine = OrderLine.restore(
			orderLineId,
			orderId,
			1L,
			"도브 뷰티 너리싱 바디워시",
			BigDecimal.valueOf(6_250),
			2
		);

		List<OrderLine> orderLines = List.of(
			orderLine
		);

		Order order = Order.restore(
			orderId,
			ordererUserId,
			OrderStatus.PENDING,
			BigDecimal.valueOf(12_500),
			BigDecimal.ZERO,
			BigDecimal.ZERO,
			orderLines,
			null
		);

		// mock
		given(orderRepository.findByIdWithLock(orderId))
			.willReturn(Optional.of(order));

		Command command = new Command(
			payerUserId,
			orderId,
			null,
			BigDecimal.valueOf(12_500),
			LocalDateTime.now()
		);

		// when & then
		assertThatThrownBy(() -> paymentMakeProcessor.execute(command))
			.isInstanceOf(CommerceException.class)
			.hasMessage("접근 권한이 없는 사용자입니다.");

	}

	// 작성 이유: 결제 금액과 주문 금액이 일치하는지 검증하기 위해서 작성했습니다. [쿠폰 사용 X]
	@Test
	void 결제_금액과_주문_금액이_다를_경우_예외를_발생시킨다() {
		// given
		final Long orderId = 122L;
		final Long ordererUserId = 1L;
		final Long orderLineId = 15L;
		final BigDecimal wrongPaymentAmount = BigDecimal.valueOf(10_000);
		final BigDecimal correctOrderAmount = BigDecimal.valueOf(12_500);

		OrderLine orderLine = OrderLine.restore(
			orderLineId,
			orderId,
			1L,
			"도브 뷰티 너리싱 바디워시",
			BigDecimal.valueOf(6_250),
			2
		);

		List<OrderLine> orderLines = List.of(
			orderLine
		);

		Order order = Order.restore(
			orderId,
			ordererUserId,
			OrderStatus.PENDING,
			correctOrderAmount,
			BigDecimal.ZERO,
			BigDecimal.ZERO,
			orderLines,
			null
		);

		CashEntity cash = CashEntity.builder()
			.balance(BigDecimal.valueOf(100_000))
			.userId(ordererUserId)
			.build();

		// mock
		given(orderRepository.findByIdWithLock(orderId))
			.willReturn(Optional.of(order));

		given(cashRepository.findByUserId(ordererUserId))
			.willReturn(Optional.of(cash));

		Command command = new Command(
			ordererUserId,
			orderId,
			null,
			wrongPaymentAmount,
			LocalDateTime.now()
		);

		// when & then
		assertThatThrownBy(() -> paymentMakeProcessor.execute(command))
			.isInstanceOf(CommerceException.class)
			.hasMessage("예상 결제 금액과 실제 결제 금액이 일치하지 않습니다.");
	}

	// 작성 이유: 존재하지 않는 쿠폰을 사용하려고 할 때 예외를 발생시키는지 검증하기 위해 작성했습니다.
	@Test
	void 존재하지_않는_사용자_쿠폰을_사용할_경우_예외를_발생시킨다() {
		// given
		final Long orderId = 122L;
		final Long ordererUserId = 1L;
		final Long orderLineId = 15L;
		final BigDecimal orderAmount = BigDecimal.valueOf(12_500);
		final Long nonExistUserCouponId = 999L;

		OrderLine orderLine = OrderLine.restore(
			orderLineId,
			orderId,
			1L,
			"도브 뷰티 너리싱 바디워시",
			BigDecimal.valueOf(6_250),
			2
		);

		List<OrderLine> orderLines = List.of(orderLine);

		Order order = Order.restore(
			orderId,
			ordererUserId,
			OrderStatus.PENDING,
			orderAmount,
			BigDecimal.ZERO,
			BigDecimal.ZERO,
			orderLines,
			null
		);

		CashEntity cash = CashEntity.builder()
			.balance(BigDecimal.valueOf(100_000))
			.userId(ordererUserId)
			.build();

		// mock
		given(orderRepository.findByIdWithLock(orderId))
			.willReturn(Optional.of(order));

		given(cashRepository.findByUserId(ordererUserId))
			.willReturn(Optional.of(cash));

		// 존재하지 않는 쿠폰을 가정합니다.
		given(userCouponRepository.findById(nonExistUserCouponId))
			.willReturn(Optional.empty());

		// when & then
		Command command = new Command(
			ordererUserId,
			orderId,
			nonExistUserCouponId,
			orderAmount,
			LocalDateTime.now()
		);

		assertThatThrownBy(() -> paymentMakeProcessor.execute(command))
			.isInstanceOf(CommerceException.class)
			.hasMessage("존재하지 않는 사용자 쿠폰입니다.");
	}

	// 작성 이유: 다른 사용자의 쿠폰을 사용하려고할 때 예외를 발생시키는지 검증하기 위해 작성했습니다.
	@Test
	void 다른_사용자의_쿠폰을_사용할_경우에_예외를_발생시킨다() {
		// given
		final Long orderId = 122L;
		final Long ordererUserId = 1L;
		final Long orderLineId = 15L;
		final BigDecimal expectedOrderAmount = BigDecimal.valueOf(11_250);
		final Long userCouponId = 999L;
		final Long otherUserId = 2L; // 다른 사용자의 쿠폰을 가정합니다.

		OrderLine orderLine = OrderLine.restore(
			orderLineId,
			orderId,
			1L,
			"도브 뷰티 너리싱 바디워시",
			BigDecimal.valueOf(6_250),
			2
		);

		List<OrderLine> orderLines = List.of(
			orderLine
		);

		Order order = Order.restore(
			orderId,
			ordererUserId,
			OrderStatus.PENDING,
			BigDecimal.valueOf(12_500),
			BigDecimal.ZERO,
			BigDecimal.ZERO,
			orderLines,
			null
		);

		CouponEntity coupon = CouponEntity.builder()
			.name("전품목 10% 할인")
			.discountAmount(BigDecimal.valueOf(10))
			.discountType(CouponDiscountType.PERCENT)
			.stock(100)
			.expiredAt(LocalDateTime.now().plusDays(30))
			.build();

		// 본인이 아닌 다른 사용자의 쿠폰입니다.
		UserCouponEntity userCoupon = UserCouponEntity
			.of(otherUserId, coupon, LocalDateTime.now());

		CashEntity cash = CashEntity.builder()
			.balance(BigDecimal.valueOf(100_000))
			.userId(ordererUserId)
			.build();

		// mock
		given(orderRepository.findByIdWithLock(orderId))
			.willReturn(Optional.of(order));

		given(userCouponRepository.findById(userCouponId))
			.willReturn(Optional.of(userCoupon));

		given(cashRepository.findByUserId(ordererUserId))
			.willReturn(Optional.of(cash));

		// when & then
		Command command = new Command(
			ordererUserId,
			orderId,
			userCouponId,
			expectedOrderAmount,
			LocalDateTime.now()
		);

		assertThatThrownBy(() -> paymentMakeProcessor.execute(command))
			.isInstanceOf(CommerceException.class)
			.hasMessage("접근 권한이 없는 사용자입니다.");
	}

	// 작성 이유: 사용자의 잔액이 부족할 때 예외를 발생시키는지 검증하기 위해 작성했습니다.
	@Test
	void 결제할_잔액이_부족한_경우_예외를_발생시킨다() {
		// given
		final Long orderId = 122L;
		final Long ordererUserId = 1L;
		final Long orderLineId = 15L;
		final BigDecimal expectedOrderAmount = BigDecimal.valueOf(11_250);
		final Long userCouponId = 999L;
		final BigDecimal notEnoughBalance = BigDecimal.valueOf(10_000); // 부족한 잔액

		OrderLine orderLine = OrderLine.restore(
			orderLineId,
			orderId,
			1L,
			"도브 뷰티 너리싱 바디워시",
			BigDecimal.valueOf(6_250),
			2
		);

		List<OrderLine> orderLines = List.of(
			orderLine
		);

		Order order = Order.restore(
			orderId,
			ordererUserId,
			OrderStatus.PENDING,
			BigDecimal.valueOf(12_500),
			BigDecimal.ZERO,
			BigDecimal.ZERO,
			orderLines,
			null
		);

		CouponEntity coupon = CouponEntity.builder()
			.name("전품목 10% 할인")
			.discountAmount(BigDecimal.valueOf(10))
			.discountType(CouponDiscountType.PERCENT)
			.stock(100)
			.expiredAt(LocalDateTime.now().plusDays(30))
			.build();

		UserCouponEntity userCoupon = UserCouponEntity
			.of(ordererUserId, coupon, LocalDateTime.now());

		CashEntity cash = CashEntity.builder()
			.balance(notEnoughBalance)
			.userId(ordererUserId)
			.build();

		// mock
		given(orderRepository.findByIdWithLock(orderId))
			.willReturn(Optional.of(order));

		given(userCouponRepository.findById(userCouponId))
			.willReturn(Optional.of(userCoupon));

		given(cashRepository.findByUserId(ordererUserId))
			.willReturn(Optional.of(cash));

		// when & then
		Command command = new Command(
			ordererUserId,
			orderId,
			userCouponId,
			expectedOrderAmount,
			LocalDateTime.now()
		);

		assertThatThrownBy(() -> paymentMakeProcessor.execute(command))
			.isInstanceOf(CommerceException.class)
			.hasMessage("잔액이 부족합니다. 잔액을 충전해주세요.");
	}

	@Test
	void 정상적으로_결제에_성공한다() {
		// given
		final Long orderId = 122L;
		final Long ordererUserId = 1L;
		final Long orderLineId = 15L;
		final BigDecimal expectedOrderAmount = BigDecimal.valueOf(11_250);
		final Long userCouponId = 999L;
		final BigDecimal sufficientBalance = BigDecimal.valueOf(100_000);

		OrderLine orderLine = OrderLine.restore(
			orderLineId,
			orderId,
			1L,
			"도브 뷰티 너리싱 바디워시",
			BigDecimal.valueOf(6_250),
			2
		);

		List<OrderLine> orderLines = List.of(
			orderLine
		);

		Order order = Order.restore(
			orderId,
			ordererUserId,
			OrderStatus.PENDING,
			BigDecimal.valueOf(12_500),
			BigDecimal.ZERO,
			BigDecimal.ZERO,
			orderLines,
			null
		);
		Order confirmedOrder = order.confirm(LocalDateTime.now());
		CouponEntity coupon = CouponEntity.builder()
			.name("전품목 10% 할인")
			.discountAmount(BigDecimal.valueOf(10))
			.discountType(CouponDiscountType.PERCENT)
			.stock(100)
			.expiredAt(LocalDateTime.now().plusDays(30))
			.build();

		UserCouponEntity userCoupon = UserCouponEntity
			.of(ordererUserId, coupon, LocalDateTime.now());

		CashEntity cash = CashEntity.builder()
			.balance(sufficientBalance)
			.userId(ordererUserId)
			.build();

		Long paymentId = 1L;
		Payment payment = Payment.restore(
			paymentId,
			ordererUserId,
			orderId,
			PaymentTargetType.ORDER,
			BigDecimal.valueOf(11_250), // 10% 할인 적용된 금액
			PaymentStatus.PAID,
			LocalDateTime.now());

		// mock
		given(orderRepository.findByIdWithLock(orderId))
			.willReturn(Optional.of(order));

		given(userCouponRepository.findById(userCouponId))
			.willReturn(Optional.of(userCoupon));

		given(cashRepository.findByUserId(ordererUserId))
			.willReturn(Optional.of(cash));

		given(cashRepository.save(cash))
			.willReturn(cash);

		given(userCouponRepository.save(userCoupon))
			.willReturn(userCoupon);

		given(orderRepository.save(order))
			.willReturn(confirmedOrder);

		given(paymentRepository.save(any(Payment.class)))
			.willReturn(payment);

		// when & then
		Command command = new Command(
			ordererUserId,
			orderId,
			userCouponId,
			expectedOrderAmount,
			LocalDateTime.now()
		);

		Output output = paymentMakeProcessor.execute(command);
		assertThat(output).isNotNull();

		CashEntity outputCash = output.cash();
		assertThat(outputCash.getBalance()).isEqualTo(BigDecimal.valueOf(88_750));

		UserCouponEntity outputUserCoupon = output.userCoupon();
		assertThat(outputUserCoupon.getLastUsedAt()).isNotNull();
		assertThat(outputUserCoupon.getOrderId()).isEqualTo(order.getId());
		assertThat(outputUserCoupon.getStatus()).isEqualTo(UserCouponStatus.USED);
		assertThat(outputUserCoupon.getId()).isEqualTo(userCoupon.getId());

		Payment outputPayment = output.payment();
		assertThat(outputPayment.getStatus()).isEqualTo(PaymentStatus.PAID);
		assertThat(outputPayment.getTargetId()).isEqualTo(order.getId());
		assertThat(outputPayment.getUserId()).isEqualTo(ordererUserId);
		assertThat(outputPayment.getAmount()).isEqualTo(BigDecimal.valueOf(11_250));
		assertThat(outputPayment.getPaidAt()).isNotNull();
	}

}

