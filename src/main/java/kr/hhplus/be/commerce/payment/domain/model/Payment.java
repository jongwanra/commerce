package kr.hhplus.be.commerce.payment.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import kr.hhplus.be.commerce.global.annotation.ImmutableObject;
import kr.hhplus.be.commerce.global.annotation.InfrastructureOnly;
import kr.hhplus.be.commerce.payment.domain.model.enums.PaymentStatus;
import kr.hhplus.be.commerce.payment.domain.model.enums.PaymentTargetType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@ImmutableObject
@Getter
public final class Payment {
	private final Long id;
	private final Long userId;
	private final Long targetId;
	private final PaymentTargetType targetType;
	private final BigDecimal amount;
	private final PaymentStatus status;
	private final LocalDateTime paidAt;

	@Builder(access = AccessLevel.PRIVATE)
	private Payment(Long id, Long userId, Long targetId, PaymentTargetType targetType, BigDecimal amount,
		PaymentStatus status, LocalDateTime paidAt) {
		this.id = id;
		this.userId = userId;
		this.targetId = targetId;
		this.targetType = targetType;
		this.amount = amount;
		this.status = status;
		this.paidAt = paidAt;
	}

	public static Payment fromOrder(Long userId, Long orderId, BigDecimal amount) {
		return Payment.builder()
			.id(0L)
			.userId(userId)
			.targetId(orderId)
			.targetType(PaymentTargetType.ORDER)
			.amount(amount)
			.status(PaymentStatus.PENDING)
			.paidAt(null)
			.build();
	}

	@InfrastructureOnly
	public static Payment restore(Long id, Long userId, Long targetId, PaymentTargetType targetType, BigDecimal amount,
		PaymentStatus status, LocalDateTime paidAt) {
		return Payment.builder()
			.id(id)
			.userId(userId)
			.targetId(targetId)
			.targetType(targetType)
			.amount(amount)
			.status(status)
			.paidAt(paidAt)
			.build();
	}

	public Payment succeed(LocalDateTime paidAt) {
		return Payment.builder()
			.id(this.id)
			.userId(this.userId)
			.targetId(this.targetId)
			.targetType(this.targetType)
			.amount(this.amount)
			.status(PaymentStatus.PAID)
			.paidAt(paidAt)
			.build();
	}

}
