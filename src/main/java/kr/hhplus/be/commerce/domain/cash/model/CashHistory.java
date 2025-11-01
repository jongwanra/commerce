package kr.hhplus.be.commerce.domain.cash.model;

import java.math.BigDecimal;

import kr.hhplus.be.commerce.domain.cash.model.enums.CashHistoryAction;
import kr.hhplus.be.commerce.domain.global.annotation.InfrastructureOnly;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record CashHistory(
	Long id,
	Long userId,
	CashHistoryAction action,
	BigDecimal balanceAfter,
	BigDecimal amount
) {

	public static CashHistory recordOfCharge(
		Long userId, BigDecimal balanceAfter, BigDecimal amount
	) {
		return CashHistory.builder()
			.userId(userId)
			.action(CashHistoryAction.CHARGE)
			.balanceAfter(balanceAfter)
			.amount(amount)
			.build();
	}

	public static CashHistory recordOfPurchase(
		Long userId, BigDecimal balanceAfter, BigDecimal amount
	) {
		return CashHistory.builder()
			.userId(userId)
			.action(CashHistoryAction.PURCHASE)
			.balanceAfter(balanceAfter)
			.amount(amount)
			.build();
	}

	public static CashHistory recordOfDeduct(
		Long userId, BigDecimal balanceAfter, BigDecimal amount
	) {
		return CashHistory.builder()
			.userId(userId)
			.action(CashHistoryAction.DEDUCT_BY_ADMIN)
			.balanceAfter(balanceAfter)
			.amount(amount)
			.build();
	}

	@InfrastructureOnly
	public static CashHistory restore(Long id, Long userId, CashHistoryAction action, BigDecimal balanceAfter,
		BigDecimal amount) {
		return CashHistory.builder()
			.id(id)
			.userId(userId)
			.action(action)
			.balanceAfter(balanceAfter)
			.amount(amount)
			.build();
	}
}
