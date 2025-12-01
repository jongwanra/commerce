package kr.hhplus.be.commerce.domain.message.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageTargetType {
	ORDER, PAYMENT, PRODUCT_RANKING
}
