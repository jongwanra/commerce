package kr.hhplus.be.commerce.domain.message.model;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import kr.hhplus.be.commerce.domain.message.enums.MessageStatus;
import kr.hhplus.be.commerce.domain.message.enums.MessageTargetType;
import kr.hhplus.be.commerce.domain.message.enums.MessageType;
import kr.hhplus.be.commerce.domain.message.model.message_payload.OrderConfirmedMessagePayload;
import kr.hhplus.be.commerce.domain.order.model.Order;
import kr.hhplus.be.commerce.domain.order.model.OrderLine;
import kr.hhplus.be.commerce.domain.order.model.enums.OrderStatus;

class MessageTest {

	// 작성 이유: 대기 상태로 메세지를 생성할 수 있는지 검증하기 위해 작성했습니다.
	@Test
	void 외부에_전송할_메세지를_대기_상태로_생성할_수_있다() {
		// given
		Long orderId = 1L;
		Order order = Order.restore(
			orderId,
			234L,
			OrderStatus.CONFIRMED,
			BigDecimal.valueOf(20_000),
			BigDecimal.ZERO,
			BigDecimal.valueOf(20_000),
			List.of(OrderLine.restore(
				3L,
				orderId,
				333L,
				"교동 짬뽕 밀키트",
				BigDecimal.valueOf(20_000),
				1
			)),
			LocalDateTime.now(),
			"ORD_ALSLMQ_OWNINSD"
		);

		// when
		Message message = Message.ofPending(
			orderId,
			MessageTargetType.ORDER,
			OrderConfirmedMessagePayload.from(order)
		);

		// then
		assertThat(message.id()).isNull();
		assertThat(message.targetId()).isEqualTo(1L);
		assertThat(message.targetType()).isEqualTo(MessageTargetType.ORDER);
		assertThat(message.type()).isEqualTo(MessageType.ORDER_CONFIRMED);
		assertThat(message.status()).isEqualTo(MessageStatus.PENDING);
		assertThat(message.publishedAt()).isNull();
		assertThat(message.payload()).isInstanceOf(OrderConfirmedMessagePayload.class);
		assertThat(message.failedAt()).isNull();
		assertThat(message.failedReason()).isBlank().as("failedReason은 초기값이 빈 문자열입니다.");
		assertThat(message.failedCount()).isZero();
	}

	// 작성 이유: 대기 상태의 메세지를 발행했을 때 상태를 검증하기 위해 작성했습니다.
	@Test
	void 대기_상태의_메세지를_발행할_수_있다() {
		// given
		Long orderId = 1L;
		Order order = Order.restore(
			orderId,
			234L,
			OrderStatus.CONFIRMED,
			BigDecimal.valueOf(20_000),
			BigDecimal.ZERO,
			BigDecimal.valueOf(20_000),
			List.of(OrderLine.restore(
				3L,
				orderId,
				333L,
				"교동 짬뽕 밀키트",
				BigDecimal.valueOf(20_000),
				1
			)),
			LocalDateTime.now(),
			"ORD_ALSLMQ_OWNINSD"
		);

		LocalDateTime now = LocalDateTime.now();
		// when
		Message message = Message.ofPending(
				orderId,
				MessageTargetType.ORDER,
				OrderConfirmedMessagePayload.from(order)
			)
			.published(now);

		// then
		assertThat(message.targetId()).isEqualTo(1L);
		assertThat(message.targetType()).isEqualTo(MessageTargetType.ORDER);
		assertThat(message.type()).isEqualTo(MessageType.ORDER_CONFIRMED);
		assertThat(message.status()).isEqualTo(MessageStatus.PUBLISHED);
		assertThat(message.publishedAt()).isEqualTo(now);
		assertThat(message.payload()).isInstanceOf(OrderConfirmedMessagePayload.class);
		assertThat(message.failedAt()).isNull();
		assertThat(message.failedCount()).isZero();
		assertThat(message.failedReason()).isBlank().as("failedReason은 초기값이 빈 문자열입니다.");
	}

	// 작성 이유: 대기 상태의 메세지 발행에 실패했을 때의 상태를 검증하기 위해 작성했습니다.
	@Test
	void 대기_상태의_메시지_발행에_실패할_수_있다() {
		// given
		Long orderId = 1L;
		Order order = Order.restore(
			orderId,
			234L,
			OrderStatus.CONFIRMED,
			BigDecimal.valueOf(20_000),
			BigDecimal.ZERO,
			BigDecimal.valueOf(20_000),
			List.of(OrderLine.restore(
				3L,
				orderId,
				333L,
				"교동 짬뽕 밀키트",
				BigDecimal.valueOf(20_000),
				1
			)),
			LocalDateTime.now(),
			"ORD_ALSLMQ_OWNINSD"
		);

		LocalDateTime now = LocalDateTime.now();
		// when
		Message message = Message.ofPending(
				orderId,
				MessageTargetType.ORDER,
				OrderConfirmedMessagePayload.from(order)
			)
			.failed("Timeout Exceed", now);

		// then
		assertThat(message.targetId()).isEqualTo(1L);
		assertThat(message.targetType()).isEqualTo(MessageTargetType.ORDER);
		assertThat(message.type()).isEqualTo(MessageType.ORDER_CONFIRMED);
		assertThat(message.status()).isEqualTo(MessageStatus.FAILED);
		assertThat(message.publishedAt()).isNull();
		assertThat(message.payload()).isInstanceOf(OrderConfirmedMessagePayload.class);
		assertThat(message.failedAt()).isEqualTo(now);
		assertThat(message.failedCount()).isOne().as("실패할 경우 카운트가 1 오른다");
		assertThat(message.failedReason()).isEqualTo("Timeout Exceed");
	}

	/**
	 *  작성 이유: 발행에 실패한 메세지는 최대 3번 까지 재시도 가능 여부를 검증하고자 작성했습니다.
	 *  (3번 시도한 이후의 Message는 DEAD_LETTER 상태가 됩니다.)
	 */
	@Test
	void 최대_3번까지_재시도할_수_있다() {
		// given
		Long orderId = 1L;
		Order order = Order.restore(
			orderId,
			234L,
			OrderStatus.CONFIRMED,
			BigDecimal.valueOf(20_000),
			BigDecimal.ZERO,
			BigDecimal.valueOf(20_000),
			List.of(OrderLine.restore(
				3L,
				orderId,
				333L,
				"교동 짬뽕 밀키트",
				BigDecimal.valueOf(20_000),
				1
			)),
			LocalDateTime.now(),
			"ORD_ALSLMQ_OWNINSD"
		);

		LocalDateTime now = LocalDateTime.now();
		Message message = Message.ofPending(
			orderId,
			MessageTargetType.ORDER,
			OrderConfirmedMessagePayload.from(order)
		);

		// when
		for (int retryCount = 1; retryCount <= 3; retryCount++) {
			message = message
				.failed("Timeout Exceed", now);
		}

		// then
		assertThat(message.targetId()).isEqualTo(1L);
		assertThat(message.targetType()).isEqualTo(MessageTargetType.ORDER);
		assertThat(message.type()).isEqualTo(MessageType.ORDER_CONFIRMED);
		assertThat(message.status()).isEqualTo(MessageStatus.DEAD_LETTER);
		assertThat(message.publishedAt()).isNull();
		assertThat(message.payload()).isInstanceOf(OrderConfirmedMessagePayload.class);
		assertThat(message.failedAt()).isEqualTo(now);
		assertThat(message.failedCount()).isEqualTo(3).as("최대 3번까지 재시도 가능합니다.");
		assertThat(message.failedReason()).isEqualTo("Timeout Exceed");
	}
}
