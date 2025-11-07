package kr.hhplus.be.commerce.infrastructure.global.lock;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import kr.hhplus.be.commerce.application.coupon.UserCouponIssueProcessor;

class CommerceSpringELParserTest {

	@Test
	void 동적으로_쿠폰의_분산락_키값을_생성할_수_있다() {
		// given
		String[] parameterNames = new String[] {"command"};
		Object[] args = new Object[] {new UserCouponIssueProcessor.Command(
			13L,
			1L,
			LocalDateTime.now()
		)};
		final String key = "'coupon:' + #command.couponId()";
		// when
		final String parsedKey = (String)CommerceSpringELParser.parse(parameterNames, args, key);
		// then
		assertThat(parsedKey).isEqualTo("coupon:1");
	}
}
