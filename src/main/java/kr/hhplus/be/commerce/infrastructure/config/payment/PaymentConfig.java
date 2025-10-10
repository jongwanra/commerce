package kr.hhplus.be.commerce.infrastructure.config.payment;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import kr.hhplus.be.commerce.domain.payment.repository.PaymentRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.payment.PaymentJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.payment.PaymentRepositoryImpl;

@Configuration
public class PaymentConfig {
	@Bean
	public PaymentRepository paymentRepository(PaymentJpaRepository paymentJpaRepository) {
		return new PaymentRepositoryImpl(paymentJpaRepository);
	}
}
