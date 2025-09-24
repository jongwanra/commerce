package kr.hhplus.be.commerce.domain.payment.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 현재 결제 실패 상태를 갖게 되는 케이스는 존재하지 않습니다.
 * 추후 다른 결제 방식이 추가될 경우 결제 실패 상태가 추가될 수 있습니다.
 */
@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
	PENDING("결제 대기"),
	PAID("결제됨");

	private final String description;
}
