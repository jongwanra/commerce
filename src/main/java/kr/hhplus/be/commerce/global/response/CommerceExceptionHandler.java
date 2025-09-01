package kr.hhplus.be.commerce.global.response;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import kr.hhplus.be.commerce.global.exception.CommerceException;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class CommerceExceptionHandler {
	@ExceptionHandler(CommerceException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public CommerceResponse<EmptyResponse> handleCommerceException(CommerceException e) {
		return CommerceResponse.fail(e.getErrorCode());
	}
}
