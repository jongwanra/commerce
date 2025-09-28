package kr.hhplus.be.commerce.infrastructure.config.payment;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import kr.hhplus.be.commerce.application.payment.PaymentMakeProcessor;
import kr.hhplus.be.commerce.domain.event.publisher.EventPublisher;
import kr.hhplus.be.commerce.domain.order.repository.OrderRepository;
import kr.hhplus.be.commerce.domain.payment.repository.PaymentRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashHistoryRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.UserCouponRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.payment.PaymentJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.payment.PaymentRepositoryImpl;

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
		CashHistoryRepository cashHistoryRepository,
		EventPublisher eventPublisher
	) {
		return new PaymentMakeProcessor(
			paymentRepository,
			orderRepository,
			userCouponRepository,
			cashRepository,
			cashHistoryRepository,
			eventPublisher
		);

	}
}
