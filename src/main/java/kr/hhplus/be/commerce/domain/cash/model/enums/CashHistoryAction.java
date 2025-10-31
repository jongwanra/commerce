package kr.hhplus.be.commerce.domain.cash.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CashHistoryAction {
	CHARGE("충전한 경우"),
	DEDUCT_BY_ADMIN("어드민에 의해 잔액이 차감된 경우"),
	PURCHASE("상품을 구매한 경우");

	private final String description;
}
