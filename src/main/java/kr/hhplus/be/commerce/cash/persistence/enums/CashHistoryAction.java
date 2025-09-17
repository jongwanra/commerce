package kr.hhplus.be.commerce.cash.persistence.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CashHistoryAction {
	CHARGE("충전한 경우"),
	PURCHASE("상품을 구매한 경우");

	private final String description;
}
