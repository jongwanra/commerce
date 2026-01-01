package kr.hhplus.be.commerce.domain.message.model.message_payload;

import java.time.LocalDateTime;

import kr.hhplus.be.commerce.domain.message.enums.MessageType;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record UserCouponIssuedMessagePayload(
	MessageType type,
	LocalDateTime now,
	long userId,
	long couponId
) implements MessagePayload {

	public static UserCouponIssuedMessagePayload of(LocalDateTime now, long userId, long couponId) {
		return UserCouponIssuedMessagePayload.builder()
			.now(now)
			.type(MessageType.USER_COUPON_ISSUED)
			.userId(userId)
			.couponId(couponId)
			.build();
	}
}
