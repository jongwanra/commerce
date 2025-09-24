package kr.hhplus.be.commerce.infrastructure.persistence.cash.entity;

import static kr.hhplus.be.commerce.domain.global.exception.CommerceCode.*;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.infrastructure.persistence.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "cash")
public class CashEntity extends BaseTimeEntity {
	private static final BigDecimal MAX_ONCE_CHARGE_AMOUNT = BigDecimal.valueOf(10_000_000);

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;

	private BigDecimal balance;

	@Builder
	private CashEntity(Long userId, BigDecimal balance) {
		this.userId = userId;
		this.balance = balance;
	}

	public void charge(BigDecimal amount) {
		validateAmountIsPositive(amount);
		validateAmountExceedsMaxOnceChargeLimit(amount);

		this.balance = this.balance.add(amount);
	}

	public void use(BigDecimal amount) {
		validateAmountIsPositive(amount);
		validateSufficientBalance(amount);

		this.balance = this.balance.subtract(amount);
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
