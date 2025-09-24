package kr.hhplus.be.commerce.cash.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.hhplus.be.commerce.cash.persistence.enums.CashHistoryAction;
import kr.hhplus.be.commerce.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "cash_history")
@ToString
public class CashHistoryEntity extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;

	@Enumerated(EnumType.STRING)
	private CashHistoryAction action;

	private BigDecimal balanceAfter;

	private BigDecimal amount;

	public static CashHistoryEntity recordOfCharge(
		Long userId,
		BigDecimal balanceAfter,
		BigDecimal amount
	) {
		CashHistoryEntity entity = new CashHistoryEntity();
		entity.userId = userId;
		entity.action = CashHistoryAction.CHARGE;
		entity.balanceAfter = balanceAfter;
		entity.amount = amount;
		return entity;
	}

	public static CashHistoryEntity recordOfPurchase(
		Long userId,
		BigDecimal balanceAfter,
		BigDecimal amount
	) {
		CashHistoryEntity entity = new CashHistoryEntity();
		entity.userId = userId;
		entity.action = CashHistoryAction.PURCHASE;
		entity.balanceAfter = balanceAfter;
		entity.amount = amount;
		return entity;
	}
}
