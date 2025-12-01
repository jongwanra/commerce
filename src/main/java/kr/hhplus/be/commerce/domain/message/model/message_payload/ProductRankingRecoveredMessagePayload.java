package kr.hhplus.be.commerce.domain.message.model.message_payload;

import java.time.LocalDate;

import kr.hhplus.be.commerce.domain.message.enums.MessageType;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record ProductRankingRecoveredMessagePayload(
	MessageType type,
	LocalDate rankingDate
) implements MessagePayload {

	public static MessagePayload from(LocalDate rankingDate) {
		return ProductRankingRecoveredMessagePayload.builder()
			.type(MessageType.PRODUCT_RANKING_RECOVERED)
			.rankingDate(rankingDate)
			.build();
	}
}
