package kr.hhplus.be.commerce.infrastructure.persistence.user.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
	ACTIVE("활성화"),
	DELETED("삭제됨");

	private final String description;
}
