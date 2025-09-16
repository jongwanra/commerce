package kr.hhplus.be.commerce.payment.infrastructure.persistence;

import org.springframework.stereotype.Repository;

import kr.hhplus.be.commerce.payment.domain.model.Payment;
import kr.hhplus.be.commerce.payment.domain.repository.PaymentRepository;
import kr.hhplus.be.commerce.payment.infrastructure.persistence.entity.PaymentEntity;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {
	private final PaymentJpaRepository paymentJpaRepository;

	@Override
	public Payment save(Payment payment) {
		return paymentJpaRepository.save(PaymentEntity.fromDomain(payment)).toDomain();
	}
}
