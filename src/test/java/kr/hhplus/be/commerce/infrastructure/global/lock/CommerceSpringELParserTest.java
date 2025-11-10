package kr.hhplus.be.commerce.infrastructure.global.lock;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.bytebuddy.utility.RandomString;

import kr.hhplus.be.commerce.application.coupon.UserCouponIssueProcessor;
import kr.hhplus.be.commerce.application.order.OrderPlaceProcessor;

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
		final String key = "#command.couponId()";
		// when
		final String parsedKey = CommerceSpringELParser.parse(parameterNames, args, key).toString();
		// then
		assertThat(parsedKey).isEqualTo("1");
	}

	@Test
	void 여러_상품의_아이디값들을_파싱할_수_있다() {
		// given
		String[] parameterNames = new String[] {"command"};
		Object[] args = new Object[] {new OrderPlaceProcessor.Command(
			RandomString.make(),
			1L,
			null,
			BigDecimal.valueOf(10_000),
			LocalDateTime.now(),
			List.of(
				new OrderPlaceProcessor.OrderLineCommand(
					1L,
					3
				),
				new OrderPlaceProcessor.OrderLineCommand(
					2L,
					7
				)
			)
		)};
		final String key = "#command.toProductIds()";
		// when
		final String parsedKey = CommerceSpringELParser.parse(parameterNames, args, key).toString();
		// then
		assertThat(parsedKey).isEqualTo("1,2");
	}
}
