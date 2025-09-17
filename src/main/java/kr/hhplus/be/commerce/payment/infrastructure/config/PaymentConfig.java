package kr.hhplus.be.commerce.payment.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import kr.hhplus.be.commerce.cash.persistence.CashHistoryRepository;
import kr.hhplus.be.commerce.cash.persistence.CashRepository;
import kr.hhplus.be.commerce.coupon.persistence.UserCouponRepository;
import kr.hhplus.be.commerce.order.domain.repository.OrderRepository;
import kr.hhplus.be.commerce.payment.application.PaymentMakeProcessor;
import kr.hhplus.be.commerce.payment.domain.repository.PaymentRepository;
import kr.hhplus.be.commerce.payment.infrastructure.persistence.PaymentJpaRepository;
import kr.hhplus.be.commerce.payment.infrastructure.persistence.PaymentRepositoryImpl;

@Configuration
public class PaymentConfig {
	@Bean
	public PaymentRepository paymentRepository(PaymentJpaRepository paymentJpaRepository) {
		return new PaymentRepositoryImpl(paymentJpaRepository);
	}

	@Bean
	public PaymentMakeProcessor paymentMakeProcessor(
		PaymentRepository paymentRepository,
		OrderRepository orderRepository,
		UserCouponRepository userCouponRepository,
		CashRepository cashRepository,
		CashHistoryRepository cashHistoryRepository
	) {
		return new PaymentMakeProcessor(
			paymentRepository,
			orderRepository,
			userCouponRepository,
			cashRepository,
			cashHistoryRepository
		);

	}
}
