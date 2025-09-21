package kr.hhplus.be.commerce.payment.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.hhplus.be.commerce.global.entity.BaseTimeEntity;
import kr.hhplus.be.commerce.payment.domain.model.Payment;
import kr.hhplus.be.commerce.payment.domain.model.enums.PaymentStatus;
import kr.hhplus.be.commerce.payment.domain.model.enums.PaymentTargetType;
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

	private Long userId;

	private Long targetId;

	@Enumerated(EnumType.STRING)
	private PaymentTargetType targetType;

	BigDecimal amount;

	@Enumerated(EnumType.STRING)
	private PaymentStatus status;

	LocalDateTime paidAt;

	@Builder
	private PaymentEntity(Long id, Long userId, Long targetId, PaymentTargetType targetType, BigDecimal amount,
		PaymentStatus status, LocalDateTime paidAt) {
		this.id = id;
		this.userId = userId;
		this.targetId = targetId;
		this.targetType = targetType;
		this.amount = amount;
		this.status = status;
		this.paidAt = paidAt;
	}

	public static PaymentEntity fromDomain(Payment payment) {
		return PaymentEntity.builder()
			.id(payment.getId())
			.amount(payment.getAmount())
			.targetId(payment.getTargetId())
			.targetType(payment.getTargetType())
			.userId(payment.getUserId())
			.status(payment.getStatus())
			.paidAt(payment.getPaidAt())
			.build();
	}
	
}
