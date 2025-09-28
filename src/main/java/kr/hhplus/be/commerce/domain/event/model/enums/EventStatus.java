package kr.hhplus.be.commerce.domain.event.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventStatus {
	PENDING, SENT, FAILED
}
