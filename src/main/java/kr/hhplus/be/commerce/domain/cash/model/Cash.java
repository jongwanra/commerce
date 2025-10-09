package kr.hhplus.be.commerce.domain.cash.model;

import static kr.hhplus.be.commerce.domain.global.exception.CommerceCode.*;

import java.math.BigDecimal;

import kr.hhplus.be.commerce.domain.global.annotation.InfrastructureOnly;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record Cash(
	Long id,
	Long userId,
	BigDecimal balance
) {
	private static final BigDecimal MAX_ONCE_CHARGE_AMOUNT = BigDecimal.valueOf(10_000_000);

	@InfrastructureOnly
	public static Cash restore(Long id, Long userId, BigDecimal balance) {
		return Cash.builder()
			.id(id)
			.userId(userId)
			.balance(balance)
			.build();
	}

	public Cash charge(BigDecimal amount) {
		validateAmountIsPositive(amount);
		validateAmountExceedsMaxOnceChargeLimit(amount);

		return Cash.builder()
			.id(this.id)
			.userId(this.userId)
			.balance(this.balance.add(amount))
			.build();
	}

	public Cash use(BigDecimal amount) {
		validateAmountIsPositive(amount);
		validateSufficientBalance(amount);

		return Cash.builder()
			.id(this.id)
			.userId(this.userId)
			.balance(this.balance.subtract(amount))
			.build();

	}

	private void validateSufficientBalance(BigDecimal amount) {
		if (this.balance.compareTo(amount) < 0) {
			throw new CommerceException(INSUFFICIENT_CASH);
		}
	}

	private void validateAmountExceedsMaxOnceChargeLimit(BigDecimal amount) {
		if (MAX_ONCE_CHARGE_AMOUNT.compareTo(amount) < 0) {
			final String formattedMaxOnceChargeAmount = String.format("%,d", MAX_ONCE_CHARGE_AMOUNT.longValue());
			throw new CommerceException(CHARGE_AMOUNT_PER_ONCE_EXCEEDS_LIMIT, formattedMaxOnceChargeAmount);
		}
	}

	private void validateAmountIsPositive(BigDecimal amount) {
		if (amount.compareTo(BigDecimal.ZERO) < 1) {
			throw new CommerceException(AMOUNT_MUST_BE_POSITIVE);
		}
	}
}
