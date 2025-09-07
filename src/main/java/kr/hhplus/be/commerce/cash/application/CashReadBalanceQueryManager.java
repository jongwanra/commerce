package kr.hhplus.be.commerce.cash.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.commerce.cash.persistence.CashJpaRepository;
import kr.hhplus.be.commerce.cash.persistence.entity.CashEntity;
import kr.hhplus.be.commerce.cash.presentation.response.CashDetailResponse;
import kr.hhplus.be.commerce.global.exception.CommerceCode;
import kr.hhplus.be.commerce.global.exception.CommerceException;
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
