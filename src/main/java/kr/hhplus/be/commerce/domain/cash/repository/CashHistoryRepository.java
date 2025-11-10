package kr.hhplus.be.commerce.domain.cash.repository;

import java.util.List;

import kr.hhplus.be.commerce.domain.cash.model.CashHistory;

public interface CashHistoryRepository {
	CashHistory save(CashHistory cashHistory);

	List<CashHistory> findAllByUserId(Long userId);
}
