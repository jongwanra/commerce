package kr.hhplus.be.commerce.payment.application;

import static java.util.Objects.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.commerce.cash.persistence.CashEntity;
import kr.hhplus.be.commerce.cash.persistence.CashHistoryEntity;
import kr.hhplus.be.commerce.cash.persistence.CashHistoryRepository;
import kr.hhplus.be.commerce.cash.persistence.CashRepository;
import kr.hhplus.be.commerce.coupon.persistence.UserCouponEntity;
import kr.hhplus.be.commerce.coupon.persistence.UserCouponRepository;
import kr.hhplus.be.commerce.global.exception.CommerceCode;
import kr.hhplus.be.commerce.global.exception.CommerceException;
import kr.hhplus.be.commerce.order.domain.model.Order;
import kr.hhplus.be.commerce.order.domain.repository.OrderRepository;
import kr.hhplus.be.commerce.payment.domain.model.Payment;
import kr.hhplus.be.commerce.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;

/**
 * expectedPaymentAmount를 클라이언트로 부터 받는 이유
 * - 사용자가 결제 페이지에서 최종적으로 확인한 금액과 다른 금액이 실제로 결제되는 것을 방지하기 위함입니다.
 */
@RequiredArgsConstructor
public class PaymentMakeProcessor {
	private final PaymentRepository paymentRepository;
	private final OrderRepository orderRepository;
	private final UserCouponRepository userCouponRepository;
	private final CashRepository cashRepository;
	private final CashHistoryRepository cashHistoryRepository;

	@Transactional
	public Output execute(Command command) {
		Order order = orderRepository.findByIdWithLock(command.orderId)
			.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_ORDER));

		order.authorize(command.userId);

		CashEntity cash = cashRepository.findByUserId(command.userId)
			.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_CASH));

		Optional<UserCouponEntity> userCouponOpt =
			isNull(command.userCouponId) ? Optional.empty() :
				Optional.of(userCouponRepository.findById(command.userCouponId)
					.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_USER_COUPON)));

		return userCouponOpt.map(userCoupon -> executeWithCoupon(command, order, cash, userCoupon))
			.orElseGet(() -> executeWithoutCoupon(command, order, cash));

	}

	private Output executeWithoutCoupon(Command command, Order order, CashEntity cash) {
		validatePaymentAmountIsMatched(command.paymentAmount, order);

		BigDecimal originalBalance = cash.getBalance();
		cash.use(command.paymentAmount);

		Payment payment = Payment.fromOrder(command.userId, command.orderId, command.paymentAmount)
			.succeed(command.now);

		order.confirm(command.now);

		cashHistoryRepository.save(
			CashHistoryEntity.recordOfPurchase(command.userId, cash.getBalance(), originalBalance));

		return new Output(
			cashRepository.save(cash),
			null,
			paymentRepository.save(payment),
			orderRepository.save(order)
		);
	}

	private Output executeWithCoupon(Command command, Order order, CashEntity cash, UserCouponEntity userCoupon) {
		validatePaymentAmountIsMatched(command.paymentAmount, userCoupon, order);
		userCoupon.use(command.userId, command.now, order.getId());

		BigDecimal originalBalance = cash.getBalance();
		cash.use(command.paymentAmount);

		Payment payment = Payment.fromOrder(command.userId, command.orderId, command.paymentAmount)
			.succeed(command.now);

		order.confirm(command.now, userCoupon);

		cashHistoryRepository.save(
			CashHistoryEntity.recordOfPurchase(command.userId, cash.getBalance(), originalBalance));

		return new Output(
			cashRepository.save(cash),
			userCouponRepository.save(userCoupon),
			paymentRepository.save(payment),
			orderRepository.save(order)
		);
	}

	private void validatePaymentAmountIsMatched(BigDecimal paymentAmount, UserCouponEntity userCoupon,
		Order order) {
		BigDecimal actualPaymentAmount = userCoupon.calculateFinalAmount(order.getAmount());
		if (paymentAmount.compareTo(actualPaymentAmount) != 0) {
			throw new CommerceException(CommerceCode.MISMATCHED_EXPECTED_AMOUNT);
		}
	}

	private void validatePaymentAmountIsMatched(BigDecimal paymentAmount, Order order) {
		BigDecimal actualPaymentAmount = order.getAmount();
		if (paymentAmount.compareTo(actualPaymentAmount) != 0) {
			throw new CommerceException(CommerceCode.MISMATCHED_EXPECTED_AMOUNT);
		}
	}

	public record Command(
		Long userId,
		Long orderId,
		Long userCouponId,
		BigDecimal paymentAmount,
		LocalDateTime now
	) {
	}

	public record Output(
		CashEntity cash,
		UserCouponEntity userCoupon,
		Payment payment,
		Order order

	) {
	}
}
