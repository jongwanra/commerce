package kr.hhplus.be.commerce.global.response;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import kr.hhplus.be.commerce.global.exception.CommerceCode;
import kr.hhplus.be.commerce.global.exception.CommerceException;
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

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public CommerceResponse<EmptyResponse> handleException(Exception e) {
		log.error("Unknown error occurred", e);
		return CommerceResponse.fail(CommerceCode.UNKNOWN_ERROR);
	}
}
