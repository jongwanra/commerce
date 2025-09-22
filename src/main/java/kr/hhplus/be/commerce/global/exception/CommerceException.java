package kr.hhplus.be.commerce.global.exception;

import lombok.Getter;

@Getter
public class CommerceException extends RuntimeException {
	private final CommerceCode commerceCode;

	public CommerceException(CommerceCode commerceCode) {
		super(commerceCode.getMessage());
		this.commerceCode = commerceCode;
	}

	public CommerceException(CommerceCode commerceCode, Object... args) {
		super(String.format(commerceCode.getMessage(), args));
		this.commerceCode = commerceCode;
	}

	public CommerceException(String message) {
		super(message);
		this.commerceCode = CommerceCode.BAD_REQUEST;
	}
}
