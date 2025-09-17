package kr.hhplus.be.commerce.payment.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import kr.hhplus.be.commerce.payment.domain.model.enums.PaymentStatus;
import kr.hhplus.be.commerce.payment.domain.model.enums.PaymentTargetType;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Payment {
	private Long id;
	private Long userId;
	private Long targetId;
	private PaymentTargetType targetType;
	private BigDecimal amount;
	private PaymentStatus status;
	private LocalDateTime paidAt;

	@Builder
	private Payment(Long userId, Long targetId, PaymentTargetType targetType, BigDecimal amount,
		PaymentStatus status, LocalDateTime paidAt) {
		this.userId = userId;
		this.targetId = targetId;
		this.targetType = targetType;
		this.amount = amount;
		this.status = status;
		this.paidAt = paidAt;
	}

	public static Payment fromOrder(Long userId, Long orderId, BigDecimal amount) {
		return Payment.builder()
			.userId(userId)
			.targetId(orderId)
			.targetType(PaymentTargetType.ORDER)
			.amount(amount)
			.status(PaymentStatus.PENDING)
			.paidAt(null)
			.build();
	}

	public void succeed(LocalDateTime paidAt) {
		this.status = PaymentStatus.PAID;
		this.paidAt = paidAt;
	}

	// infrastructure에서만 접근 가능합니다.
	public void assignId(Long id) {
		this.id = id;
	}

}
