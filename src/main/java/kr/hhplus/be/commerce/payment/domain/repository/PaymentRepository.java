package kr.hhplus.be.commerce.payment.domain.repository;

import kr.hhplus.be.commerce.payment.domain.model.Payment;

public interface PaymentRepository {
	Payment save(Payment payment);
}
