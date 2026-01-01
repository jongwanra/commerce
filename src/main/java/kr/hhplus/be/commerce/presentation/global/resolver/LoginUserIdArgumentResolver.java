package kr.hhplus.be.commerce.presentation.global.resolver;

import static java.util.Objects.*;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import kr.hhplus.be.commerce.presentation.global.annotation.LoginUserId;
import lombok.RequiredArgsConstructor;

/**
 * 인증 프로세스가 구현이 안되어 있습니다.
 * 부하 테스트를 할 수 있도록 우선 Header를 통해 UserId값을 직접적으로 넣는 방식을 채택했습니다.
 */
@Component
@RequiredArgsConstructor
public class LoginUserIdArgumentResolver implements HandlerMethodArgumentResolver {
	private static final String HEADER_NAME = "Commerce-User-Id";

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(LoginUserId.class);
	}

	@Override
	public Long resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

		final String userIdText = webRequest.getHeader(HEADER_NAME);
		if (isNull(userIdText)) {
			return 1L;
		}

		return Long.parseLong(userIdText);
	}

}
