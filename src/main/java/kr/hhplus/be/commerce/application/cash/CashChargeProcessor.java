package kr.hhplus.be.commerce.application.cash;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.commerce.domain.cash.model.Cash;
import kr.hhplus.be.commerce.domain.cash.model.CashHistory;
import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashHistoryRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CashChargeProcessor {
	private final CashRepository cashRepository;
	private final CashHistoryRepository cashHistoryRepository;

	@Transactional
	public Output execute(Command command) {
		Cash originalCash = cashRepository.findByUserId(command.userId())
			.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_CASH));

		Cash chargedCash = originalCash.charge(command.amount);

		cashRepository.save(chargedCash);
		cashHistoryRepository.save(
			CashHistory.recordOfCharge(
				chargedCash.userId(),
				chargedCash.balance(),
				command.amount()
			)
		);

		return new Output(
			chargedCash.userId(),
			originalCash.balance(),
			chargedCash.balance()
		);
	}

	public record Command(
		Long userId,
		BigDecimal amount
	) {
	}

	public record Output(
		Long userId,
		BigDecimal originalBalance,
		BigDecimal newBalance
	) {
	}
}
