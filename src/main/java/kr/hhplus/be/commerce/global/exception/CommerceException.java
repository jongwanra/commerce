package kr.hhplus.be.commerce.global.exception;

import lombok.Getter;

@Getter
public class CommerceException extends RuntimeException {
	private final ErrorCode errorCode;

	public CommerceException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
