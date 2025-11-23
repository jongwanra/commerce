package kr.hhplus.be.commerce.application.message.publisher;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import kr.hhplus.be.commerce.domain.message.enums.MessageType;
import kr.hhplus.be.commerce.domain.message.model.message_payload.ProductRankingRecoveredMessagePayload;
import kr.hhplus.be.commerce.domain.product_ranking.model.ProductRanking;
import kr.hhplus.be.commerce.domain.product_ranking.repository.ProductRankingRepository;
import kr.hhplus.be.commerce.domain.product_ranking.store.ProductRankingStore;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductRankingRecoveredMessagePublisher
	implements MessagePublisher<ProductRankingRecoveredMessagePayload> {
	private final ProductRankingRepository productRankingRepository;
	private final ProductRankingStore productRankingStore;

	@Override
	public MessageType getSupportingMessageType() {
		return MessageType.PRODUCT_RANKING_RECOVERED;
	}

	@Override
	public void publish(ProductRankingRecoveredMessagePayload messagePayload) {
		List<ProductRanking> productRankings = productRankingRepository.findAllByRankingDate(
			messagePayload.rankingDate());

		productRankingStore.bulkInsert(productRankings, messagePayload.rankingDate(), LocalDateTime.now());

	}
}
