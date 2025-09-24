package kr.hhplus.be.commerce.domain.order.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
	PENDING("주문 대기 중"),
	CONFIRMED("주문 확정"),
	CANCELLED("주문 취소됨"),
	;
	private final String description;

	public boolean isConfirmed() {
		return this == CONFIRMED;
	}
}
