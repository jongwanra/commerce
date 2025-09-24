package kr.hhplus.be.commerce.infrastructure.persistence.order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.hhplus.be.commerce.infrastructure.persistence.order.entity.OrderLineEntity;

public interface OrderLineJpaRepository extends JpaRepository<OrderLineEntity, Long> {
	List<OrderLineEntity> findAllByOrderId(Long orderId);
}
