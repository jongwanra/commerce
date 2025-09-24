package kr.hhplus.be.commerce.presentation.global.response;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class CommerceExceptionHandler {
	@ExceptionHandler(CommerceException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public CommerceResponse<EmptyResponse> handleCommerceException(CommerceException e) {
		return CommerceResponse.fail(e.getCommerceCode());
	}

	// 유효하지 않은 값으로 클라이언트가 API를 호출할 경우 발생하는 예외를 관리합니다.
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public CommerceResponse<EmptyResponse> handleValidationException(MethodArgumentNotValidException e) {
		final String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
		return CommerceResponse.fail(CommerceCode.BAD_REQUEST.getCode(), errorMessage);
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public CommerceResponse<EmptyResponse> handleException(Exception e) {
		log.error("Unknown error occurred", e);
		return CommerceResponse.fail(CommerceCode.UNKNOWN_ERROR.getCode(), e.getMessage());
	}
}
