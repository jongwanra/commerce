package kr.hhplus.be.commerce.domain.payment.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentTargetType {
	ORDER("주문"),
	;;

	private final String description;
}
