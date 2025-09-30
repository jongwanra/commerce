package kr.hhplus.be.commerce.infrastructure.persistence.cash;

import java.util.List;

import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.CashHistoryEntity;

public interface CashHistoryRepository {
	CashHistoryEntity save(CashHistoryEntity cashHistoryEntity);

	List<CashHistoryEntity> findAllByUserId(Long userId);
}
