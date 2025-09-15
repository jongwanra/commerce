package kr.hhplus.be.commerce.payment.application;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import kr.hhplus.be.commerce.coupon.persistence.entity.UserCouponEntity;
import kr.hhplus.be.commerce.global.exception.CommerceCode;
import kr.hhplus.be.commerce.global.exception.CommerceException;
import kr.hhplus.be.commerce.order.domain.model.Order;

/**
 * 예상 결제 금액과 실제 결제 금액이 일치하는지 여부를 검증하는 역할을 가진 클래스입니다.
 */
@Service
public class PaymentAmountValidator {
	public void validate(BigDecimal expectedPaymentAmount, Order order, UserCouponEntity userCoupon) {
		BigDecimal actualPaymentAmount = userCoupon.calculateDiscountAmount(order.getAmount());

		if (expectedPaymentAmount.compareTo(actualPaymentAmount) != 0) {
			throw new CommerceException(CommerceCode.MISMATCHED_EXPECTED_AMOUNT);
		}
	}

	public void validate(BigDecimal expectedPaymentAmount, Order order) {
		BigDecimal actualPaymentAmount = order.getAmount();
		if (expectedPaymentAmount.compareTo(actualPaymentAmount) != 0) {
			throw new CommerceException(CommerceCode.MISMATCHED_EXPECTED_AMOUNT);
		}
	}
}
