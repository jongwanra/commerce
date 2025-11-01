package kr.hhplus.be.commerce.infrastructure.persistence.order;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import kr.hhplus.be.commerce.infrastructure.persistence.order.entity.OrderEntity;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {

	Optional<OrderEntity> findByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);
	
}
