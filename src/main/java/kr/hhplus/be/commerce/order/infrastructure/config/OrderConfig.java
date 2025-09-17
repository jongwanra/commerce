package kr.hhplus.be.commerce.order.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import kr.hhplus.be.commerce.order.application.OrderPlaceProcessor;
import kr.hhplus.be.commerce.order.domain.repository.OrderRepository;
import kr.hhplus.be.commerce.order.infrastructure.persistence.OrderJpaRepository;
import kr.hhplus.be.commerce.order.infrastructure.persistence.OrderLineJpaRepository;
import kr.hhplus.be.commerce.order.infrastructure.persistence.OrderRepositoryImpl;
import kr.hhplus.be.commerce.product.domain.repositorty.ProductRepository;

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
