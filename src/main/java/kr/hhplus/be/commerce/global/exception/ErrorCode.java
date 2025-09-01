package kr.hhplus.be.commerce.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	NOT_FOUND_USER(400,"UR-0001", "존재하지 않는 사용자입니다.");

	private final int status;
	private final String code;
	private final String message;

}
