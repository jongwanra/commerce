package kr.hhplus.be.commerce.application.product_ranking;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.commerce.domain.message.enums.MessageTargetType;
import kr.hhplus.be.commerce.domain.message.model.Message;
import kr.hhplus.be.commerce.domain.message.model.message_payload.ProductRankingRecoveredMessagePayload;
import kr.hhplus.be.commerce.domain.message.repository.MessageRepository;
import kr.hhplus.be.commerce.domain.order.model.Order;
import kr.hhplus.be.commerce.domain.order.model.OrderLine;
import kr.hhplus.be.commerce.domain.order.repository.OrderRepository;
import kr.hhplus.be.commerce.domain.product_ranking.model.ProductRanking;
import kr.hhplus.be.commerce.domain.product_ranking.repository.ProductRankingRepository;
import lombok.RequiredArgsConstructor;

/**
 * Redis 시스템 장애로 인한 상품 랭킹을 Redis와 MySQL에 복구(동기화)하는 프로세서입니다.
 * Redis 시스템 복구는 비동기적으로 처리합니다.
 * @see  kr.hhplus.be.commerce.application.message.publisher.ProductRankingRecoveredMessagePublisher
 */
@Service
@RequiredArgsConstructor
public class ProductRankingRecoverAdminProcessor {
	private final ProductRankingRepository productRankingRepository;
	private final OrderRepository orderRepository;
	private final MessageRepository messageRepository;

	@Transactional
	public void execute(Command command) {
		List<Order> orders = orderRepository.findAllDailyConfirmed(command.today);
		List<Long> productIds = orders.stream()
			.map(Order::orderLines)
			.flatMap(List::stream)
			.map(OrderLine::productId)
			.toList();

		// 판매량은, orderLines의 orderQuantity를 보고 알 수 있다.
		// TODO 판매량 증가 로직에서 orderQuantity가 아니라, 1로 증가시키고 있습니다.
		// 	고쳐야합니다.
		Map<Long, Integer> productIdToSalesCountMap = orders.stream()
			.map(Order::orderLines)
			.flatMap(List::stream)
			.collect(Collectors.toMap(OrderLine::productId, OrderLine::orderQuantity, Integer::sum));

		Map<Long, ProductRanking> originProductIdToRankingMap = productRankingRepository.findAllByRankingDate(
				command.today)
			.stream()
			.collect(Collectors.toMap(ProductRanking::productId, it -> it));

		List<ProductRanking> recoveredRankings = productIds.stream()
			.map((productId) -> originProductIdToRankingMap.getOrDefault(productId,
				ProductRanking.empty(productId, command.today())))
			.map((ranking) -> {
				final int salesCount = productIdToSalesCountMap.getOrDefault(ranking.productId(), 0);
				return ranking.renewSalesCount(salesCount);
			})
			.toList();

		productRankingRepository.saveAll(recoveredRankings);
		messageRepository.save(Message.ofPending(
				0L,
				MessageTargetType.PRODUCT_RANKING,
				ProductRankingRecoveredMessagePayload.from(command.today)
			)
		);

	}

	public record Command(
		LocalDate today
	) {
	}
}
