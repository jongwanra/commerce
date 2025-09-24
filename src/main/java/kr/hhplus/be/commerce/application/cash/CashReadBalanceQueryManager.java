package kr.hhplus.be.commerce.application.cash;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.CashEntity;
import kr.hhplus.be.commerce.presentation.api.cash.response.CashDetailResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CashReadBalanceQueryManager {
	private final CashJpaRepository cashJpaRepository;

	@Transactional(readOnly = true)
	public CashDetailResponse read(Long userId) {
		CashEntity cash = cashJpaRepository.findByUserId(userId)
			.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_CASH));

		return new CashDetailResponse(
			cash.getBalance(),
			cash.getModifiedAt()
		);
	}
}
