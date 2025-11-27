package kr.hhplus.be.commerce.infrastructure.config.order;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.persistence.EntityManager;
import kr.hhplus.be.commerce.application.order.OrderPlaceProcessor;
import kr.hhplus.be.commerce.application.order.OrderPlaceWithEventProcessor;
import kr.hhplus.be.commerce.domain.cash.repository.CashHistoryRepository;
import kr.hhplus.be.commerce.domain.cash.repository.CashRepository;
import kr.hhplus.be.commerce.domain.coupon.repository.UserCouponRepository;
import kr.hhplus.be.commerce.domain.order.repository.OrderRepository;
import kr.hhplus.be.commerce.domain.payment.repository.PaymentRepository;
import kr.hhplus.be.commerce.domain.product.repository.ProductRepository;
import kr.hhplus.be.commerce.domain.user.repository.UserRepository;
import kr.hhplus.be.commerce.infrastructure.event.SpringEventPublisher;
import kr.hhplus.be.commerce.infrastructure.persistence.order.OrderJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.order.OrderLineJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.order.OrderRepositoryImpl;

@Configuration
public class OrderConfig {
	@Bean
	public OrderRepository orderRepository(
		OrderJpaRepository orderJpaRepository,
		OrderLineJpaRepository orderLineJpaRepository,
		EntityManager entityManager
	) {
		return new OrderRepositoryImpl(
			orderJpaRepository,
			orderLineJpaRepository,
			entityManager
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
		UserRepository userRepository,
		SpringEventPublisher springEventPublisher
	) {
		return new OrderPlaceWithEventProcessor(
			orderRepository,
			paymentRepository,
			productRepository,
			userCouponRepository,
			cashRepository,
			cashHistoryRepository,
			userRepository,
			springEventPublisher
		);
	}

}
