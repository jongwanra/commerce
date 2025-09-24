package kr.hhplus.be.commerce.infrastructure.persistence.cash;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.CashHistoryEntity;

public interface CashHistoryJpaRepository extends JpaRepository<CashHistoryEntity, Long> {
	List<CashHistoryEntity> findAllByUserId(Long userId);
}
