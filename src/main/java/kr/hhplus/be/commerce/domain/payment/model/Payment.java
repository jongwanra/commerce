package kr.hhplus.be.commerce.domain.payment.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import kr.hhplus.be.commerce.domain.global.annotation.InfrastructureOnly;
import kr.hhplus.be.commerce.domain.payment.model.enums.PaymentStatus;
import kr.hhplus.be.commerce.domain.payment.model.enums.PaymentTargetType;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record Payment(
	Long id,
	String idempotencyKey,
	Long userId,
	Long targetId,
	PaymentTargetType targetType,
	BigDecimal amount,
	PaymentStatus status,
	LocalDateTime paidAt
) {

	public static Payment fromOrder(String idempotencyKey, Long userId, Long orderId, BigDecimal amount) {
		return Payment.builder()
			.id(null)
			.idempotencyKey(idempotencyKey)
			.userId(userId)
			.targetId(orderId)
			.targetType(PaymentTargetType.ORDER)
			.amount(amount)
			.status(PaymentStatus.PENDING)
			.paidAt(null)
			.build();
	}

	@InfrastructureOnly
	public static Payment restore(Long id, String idempotencyKey, Long userId, Long targetId,
		PaymentTargetType targetType, BigDecimal amount,
		PaymentStatus status, LocalDateTime paidAt) {
		return Payment.builder()
			.id(id)
			.idempotencyKey(idempotencyKey)
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
			.idempotencyKey(this.idempotencyKey)
			.userId(this.userId)
			.targetId(this.targetId)
			.targetType(this.targetType)
			.amount(this.amount)
			.status(PaymentStatus.PAID)
			.paidAt(paidAt)
			.build();
	}

}
