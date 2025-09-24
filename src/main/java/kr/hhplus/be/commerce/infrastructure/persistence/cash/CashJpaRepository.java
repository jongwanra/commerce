package kr.hhplus.be.commerce.infrastructure.persistence.cash;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.CashEntity;

public interface CashJpaRepository extends JpaRepository<CashEntity, Long> {
	Optional<CashEntity> findByUserId(Long userId);
}
