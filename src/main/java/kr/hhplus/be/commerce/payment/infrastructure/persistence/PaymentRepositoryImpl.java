package kr.hhplus.be.commerce.payment.infrastructure.persistence;

import kr.hhplus.be.commerce.payment.domain.model.Payment;
import kr.hhplus.be.commerce.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {
	private final PaymentJpaRepository paymentJpaRepository;

	@Override
	public Payment save(Payment payment) {
		return paymentJpaRepository.save(PaymentEntity.fromDomain(payment)).toDomain();
	}
}
