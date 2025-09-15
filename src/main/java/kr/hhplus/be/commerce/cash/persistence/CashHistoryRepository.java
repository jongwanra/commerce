package kr.hhplus.be.commerce.cash.persistence;

import kr.hhplus.be.commerce.cash.persistence.entity.CashHistoryEntity;

public interface CashHistoryRepository {
	CashHistoryEntity save(CashHistoryEntity cashHistoryEntity);
}
