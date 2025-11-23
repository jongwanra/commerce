package kr.hhplus.be.commerce.domain.order.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import kr.hhplus.be.commerce.domain.order.model.Order;

public interface OrderRepository {
	Order save(Order order);

	Optional<Order> findByIdempotencyKey(String idempotencyKey);

	List<Order> findAllDailyConfirmed(LocalDate date);

	Optional<Order> findById(Long orderId);
}
