package kr.hhplus.be.commerce.domain.payment.repository;

import java.util.Optional;

import kr.hhplus.be.commerce.domain.payment.model.Payment;

public interface PaymentRepository {
	Payment save(Payment payment);

	Optional<Payment> findByIdempotencyKey(String idempotencyKey);
}
