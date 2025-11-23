package kr.hhplus.be.commerce.domain.message.enums;

import kr.hhplus.be.commerce.domain.message.model.message_payload.MessagePayload;
import kr.hhplus.be.commerce.domain.message.model.message_payload.OrderConfirmedMessagePayload;
import kr.hhplus.be.commerce.domain.message.model.message_payload.ProductRankingRecoveredMessagePayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageType {
	ORDER_CONFIRMED("결제가 완료된 주문건", OrderConfirmedMessagePayload.class),
	PRODUCT_RANKING_RECOVERED("상품 랭킹 레디스 시스템 복구", ProductRankingRecoveredMessagePayload.class),
	;

	private final String description;
	private final Class<? extends MessagePayload> payloadClass;
}
