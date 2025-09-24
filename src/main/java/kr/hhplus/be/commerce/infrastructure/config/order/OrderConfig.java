package kr.hhplus.be.commerce.infrastructure.config.order;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import kr.hhplus.be.commerce.application.order.OrderPlaceProcessor;
import kr.hhplus.be.commerce.domain.order.repository.OrderRepository;
import kr.hhplus.be.commerce.domain.product.repository.ProductRepository;
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
		ProductRepository productRepository
	) {
		return new OrderPlaceProcessor(
			orderRepository,
			productRepository
		);
	}

}
