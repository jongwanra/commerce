package kr.hhplus.be.commerce.cash.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CashJpaRepository extends JpaRepository<CashEntity, Long> {
	Optional<CashEntity> findByUserId(Long userId);
}
