package kr.hhplus.be.commerce.presentation.global.resolver;

import static java.util.Objects.*;
import static kr.hhplus.be.commerce.presentation.global.utils.CommerceHttpRequestHeaderName.*;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.servlet.http.HttpServletRequest;
import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.presentation.global.annotation.IdempotencyKey;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IdempotencyKeyArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(IdempotencyKey.class);
	}

	@Override
	public String resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
		HttpServletRequest httpServletRequest = requireNonNull(
			webRequest.getNativeRequest(HttpServletRequest.class));

		final String idempotencyKey = httpServletRequest.getHeader(X_COMMERCE_IDEMPOTENCY_KEY);
		if (isNull(idempotencyKey) || idempotencyKey.isBlank()) {
			throw new CommerceException(CommerceCode.IDEMPOTENCY_KEY_IS_REQUIRED);
		}
		return idempotencyKey;
	}

}
