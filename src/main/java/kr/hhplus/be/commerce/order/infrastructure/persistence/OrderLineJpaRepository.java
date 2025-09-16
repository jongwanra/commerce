package kr.hhplus.be.commerce.order.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.hhplus.be.commerce.order.infrastructure.persistence.entity.OrderLineEntity;

public interface OrderLineJpaRepository extends JpaRepository<OrderLineEntity, Long> {
	List<OrderLineEntity> findAllByOrderId(Long orderId);
}
