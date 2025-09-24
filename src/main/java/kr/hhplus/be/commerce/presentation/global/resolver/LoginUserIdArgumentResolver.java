package kr.hhplus.be.commerce.presentation.global.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import kr.hhplus.be.commerce.presentation.global.annotation.LoginUserId;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoginUserIdArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(LoginUserId.class);
	}

	@Override
	public Long resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

		// TODO 실제 로그인 유저 식별자 로직 구현 필요
		return 1L;
	}

}
