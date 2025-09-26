package kr.hhplus.be.commerce.infrastructure.persistence.payment;

import java.util.Optional;

import kr.hhplus.be.commerce.domain.payment.model.Payment;
import kr.hhplus.be.commerce.domain.payment.repository.PaymentRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.payment.entity.PaymentEntity;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {
	private final PaymentJpaRepository paymentJpaRepository;

	@Override
	public Payment save(Payment payment) {
		return paymentJpaRepository.save(PaymentEntity.fromDomain(payment)).toDomain();
	}

	@Override
	public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
		return paymentJpaRepository.findByIdempotencyKey(idempotencyKey)
			.map(PaymentEntity::toDomain);
	}
}
