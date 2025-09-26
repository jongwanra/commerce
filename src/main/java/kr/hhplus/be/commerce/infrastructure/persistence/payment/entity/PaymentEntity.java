package kr.hhplus.be.commerce.infrastructure.persistence.payment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.hhplus.be.commerce.domain.payment.model.Payment;
import kr.hhplus.be.commerce.domain.payment.model.enums.PaymentStatus;
import kr.hhplus.be.commerce.domain.payment.model.enums.PaymentTargetType;
import kr.hhplus.be.commerce.infrastructure.persistence.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentEntity extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String idempotencyKey;

	private Long userId;

	private Long targetId;

	@Enumerated(EnumType.STRING)
	private PaymentTargetType targetType;

	BigDecimal amount;

	@Enumerated(EnumType.STRING)
	private PaymentStatus status;

	LocalDateTime paidAt;

	@Builder
	private PaymentEntity(Long id, String idempotencyKey, Long userId, Long targetId, PaymentTargetType targetType,
		BigDecimal amount,
		PaymentStatus status, LocalDateTime paidAt) {
		this.id = id;
		this.idempotencyKey = idempotencyKey;
		this.userId = userId;
		this.targetId = targetId;
		this.targetType = targetType;
		this.amount = amount;
		this.status = status;
		this.paidAt = paidAt;
	}

	public static PaymentEntity fromDomain(Payment payment) {
		return PaymentEntity.builder()
			.id(payment.id())
			.idempotencyKey(payment.idempotencyKey())
			.amount(payment.amount())
			.targetId(payment.targetId())
			.targetType(payment.targetType())
			.userId(payment.userId())
			.status(payment.status())
			.paidAt(payment.paidAt())
			.build();
	}

	public Payment toDomain() {
		return Payment.restore(
			id,
			idempotencyKey,
			userId,
			targetId,
			targetType,
			amount,
			status,
			paidAt
		);
	}
}
