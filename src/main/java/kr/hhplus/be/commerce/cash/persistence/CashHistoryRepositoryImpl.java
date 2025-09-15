package kr.hhplus.be.commerce.cash.persistence;

import org.springframework.stereotype.Repository;

import kr.hhplus.be.commerce.cash.persistence.entity.CashHistoryEntity;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CashHistoryRepositoryImpl implements CashHistoryRepository {
	private final CashHistoryJpaRepository cashHistoryJpaRepository;

	@Override
	public CashHistoryEntity save(CashHistoryEntity cashHistoryEntity) {
		return cashHistoryJpaRepository.save(cashHistoryEntity);
	}
}
