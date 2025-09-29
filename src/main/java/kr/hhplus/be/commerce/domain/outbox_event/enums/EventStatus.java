package kr.hhplus.be.commerce.domain.outbox_event.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventStatus {
	PENDING("외부 시스템으로 발행 대기중"),
	PUBLISHED("외부 시스템으로 발행 완료"),
	FAILED("발행 실패, 재시도 가능"),
	DEAD_LETTER("최대 재시도 횟수 초과로 발행 포기");

	private final String description;
}
