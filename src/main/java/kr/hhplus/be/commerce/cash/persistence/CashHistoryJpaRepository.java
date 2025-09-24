package kr.hhplus.be.commerce.cash.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CashHistoryJpaRepository extends JpaRepository<CashHistoryEntity, Long> {
	List<CashHistoryEntity> findAllByUserId(Long userId);
}
