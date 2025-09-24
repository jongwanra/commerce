package kr.hhplus.be.commerce.infrastructure.persistence.cash;

import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.CashHistoryEntity;

public interface CashHistoryRepository {
	CashHistoryEntity save(CashHistoryEntity cashHistoryEntity);
}
