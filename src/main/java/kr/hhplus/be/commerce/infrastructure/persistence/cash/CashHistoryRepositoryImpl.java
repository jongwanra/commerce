package kr.hhplus.be.commerce.infrastructure.persistence.cash;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.hhplus.be.commerce.domain.cash.model.CashHistory;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.CashHistoryEntity;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CashHistoryRepositoryImpl implements CashHistoryRepository {
	private final CashHistoryJpaRepository cashHistoryJpaRepository;

	@Override
	public CashHistory save(CashHistory cashHistory) {
		return cashHistoryJpaRepository.save(CashHistoryEntity.fromDomain(cashHistory))
			.toDomain();
	}

	@Override
	public List<CashHistory> findAllByUserId(Long userId) {
		return cashHistoryJpaRepository.findAllByUserId(userId)
			.stream()
			.map(CashHistoryEntity::toDomain)
			.toList();
	}
}
