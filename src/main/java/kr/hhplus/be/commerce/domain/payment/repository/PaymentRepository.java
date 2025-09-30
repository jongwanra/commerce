package kr.hhplus.be.commerce.domain.payment.repository;

import kr.hhplus.be.commerce.domain.payment.model.Payment;

public interface PaymentRepository {
	Payment save(Payment payment);
	
}
