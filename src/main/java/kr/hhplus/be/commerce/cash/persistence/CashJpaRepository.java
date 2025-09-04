package kr.hhplus.be.commerce.cash.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.hhplus.be.commerce.cash.persistence.entity.CashEntity;

public interface CashJpaRepository extends JpaRepository<CashEntity, Long> {
	Optional<CashEntity> findByUserId(Long userId);
}
