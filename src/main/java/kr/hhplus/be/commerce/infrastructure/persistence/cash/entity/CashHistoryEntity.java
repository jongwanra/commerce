package kr.hhplus.be.commerce.infrastructure.persistence.cash.entity;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.hhplus.be.commerce.domain.cash.model.CashHistory;
import kr.hhplus.be.commerce.domain.cash.model.enums.CashHistoryAction;
import kr.hhplus.be.commerce.infrastructure.persistence.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "cash_history")
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class CashHistoryEntity extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;

	@Enumerated(EnumType.STRING)
	private CashHistoryAction action;

	private BigDecimal balanceAfter;

	private BigDecimal amount;

	public static CashHistoryEntity fromDomain(CashHistory cashHistory) {
		return CashHistoryEntity.builder()
			.id(cashHistory.id())
			.userId(cashHistory.userId())
			.action(cashHistory.action())
			.balanceAfter(cashHistory.balanceAfter())
			.amount(cashHistory.amount())
			.build();
	}

	public CashHistory toDomain() {
		return CashHistory.restore(
			id,
			userId,
			action,
			balanceAfter,
			amount
		);
	}
}
