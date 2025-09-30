package kr.hhplus.be.commerce.application.cash;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashHistoryRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.CashEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.CashHistoryEntity;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CashChargeProcessor {
	private final CashRepository cashRepository;
	private final CashHistoryRepository cashHistoryRepository;

	@Transactional
	public Output execute(Command command) {
		CashEntity cash = cashRepository.findByUserId(command.userId())
			.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_CASH));

		BigDecimal originalBalance = cash.getBalance();
		cash.charge(command.amount);
		BigDecimal newBalance = cash.getBalance();

		cashRepository.save(cash);
		cashHistoryRepository.save(
			CashHistoryEntity.recordOfCharge(
				cash.getUserId(),
				newBalance,
				command.amount()
			)
		);

		return new Output(
			cash.getUserId(),
			originalBalance,
			newBalance
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
