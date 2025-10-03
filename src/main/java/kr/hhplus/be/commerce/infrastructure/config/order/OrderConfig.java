package kr.hhplus.be.commerce.infrastructure.config.order;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import kr.hhplus.be.commerce.application.event.publisher.EventPublisher;
import kr.hhplus.be.commerce.application.order.OrderPlaceProcessor;
import kr.hhplus.be.commerce.domain.order.repository.OrderRepository;
import kr.hhplus.be.commerce.domain.payment.repository.PaymentRepository;
import kr.hhplus.be.commerce.domain.product.repository.ProductRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashHistoryRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.UserCouponRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.order.OrderJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.order.OrderLineJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.order.OrderRepositoryImpl;

@Configuration
public class OrderConfig {
	@Bean
	public OrderRepository orderRepository(
		OrderJpaRepository orderJpaRepository,
		OrderLineJpaRepository orderLineJpaRepository
	) {
		return new OrderRepositoryImpl(
			orderJpaRepository,
			orderLineJpaRepository
		);
	}

	@Bean
	public OrderPlaceProcessor orderPlaceProcessor(
		OrderRepository orderRepository,
		PaymentRepository paymentRepository,
		ProductRepository productRepository,
		UserCouponRepository userCouponRepository,
		CashRepository cashRepository,
		CashHistoryRepository cashHistoryRepository,
		EventPublisher eventPublisher
	) {
		return new OrderPlaceProcessor(
			orderRepository,
			paymentRepository,
			productRepository,
			userCouponRepository,
			cashRepository,
			cashHistoryRepository,
			eventPublisher
		);
	}

}
