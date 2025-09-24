package kr.hhplus.be.commerce.presentation.global.response;

import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record CommerceResponse<T>(
	boolean success,
	String code,
	String message,
	T data
) {

	public static <T> CommerceResponse<T> success(T data) {
		return new CommerceResponse<>(true, CommerceCode.SUCCESS.getCode(), CommerceCode.SUCCESS.getMessage(), data);
	}

	public static CommerceResponse<EmptyResponse> fail(String code, String message) {
		return new CommerceResponse<>(false, code, message, EmptyResponse.INSTANCE);
	}

	public static CommerceResponse<EmptyResponse> fail(CommerceCode commerceCode) {
		return new CommerceResponse<>(false, commerceCode.getCode(), commerceCode.getMessage(), EmptyResponse.INSTANCE);
	}
}
