package kr.hhplus.be.commerce.domain.event.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventTargetType {
	ORDER, PAYMENT
}
