package kr.hhplus.be.commerce.global.response;

import kr.hhplus.be.commerce.global.exception.ErrorCode;

public record CommerceResponse<T>(
	boolean success,
	String errorCode,
	String errorMessage,
	T data
) {

	public static <T> CommerceResponse<T> success(T data) {
		return new CommerceResponse<>(true, null, null, data);
	}

	public static <T> CommerceResponse<T> fail(String errorCode, String errorMessage) {
		return new CommerceResponse<>(false, errorCode, errorMessage, null);
	}

	public static <T> CommerceResponse<T> fail(ErrorCode errorCode) {
		return new CommerceResponse<>(false, errorCode.getCode(), errorCode.getMessage(), null);
	}
}
