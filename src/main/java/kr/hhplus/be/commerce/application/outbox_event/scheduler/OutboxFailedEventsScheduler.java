package kr.hhplus.be.commerce.application.outbox_event.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.commerce.domain.outbox_event.enums.EventStatus;
import kr.hhplus.be.commerce.domain.outbox_event.event.Event;
import kr.hhplus.be.commerce.domain.outbox_event.mapper.EventPublisherMapping;
import kr.hhplus.be.commerce.domain.outbox_event.model.OutboxEvent;
import kr.hhplus.be.commerce.domain.outbox_event.publisher.EventPublisher;
import kr.hhplus.be.commerce.domain.outbox_event.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxFailedEventsScheduler {
	private static final int BATCH_SIZE = 100;
	private static final int ONE_MINUTE = 60 * 1000;

	private final OutboxEventRepository outboxEventRepository;
	private final EventPublisherMapping eventPublisherMapping;

	@Scheduled(fixedDelay = ONE_MINUTE)
	@Transactional
	public void execute() {
		List<OutboxEvent> outboxEvents = outboxEventRepository.findAllByStatusOrderByCreatedAtAscLimit(
				EventStatus.FAILED, BATCH_SIZE)
			.stream()
			.map((outboxEvent) -> {
				try {
					Event event = outboxEvent.toEvent();
					EventPublisher eventPublisher = eventPublisherMapping.get(event.type());
					eventPublisher.publish(event);

					return outboxEvent.published();
				} catch (Exception e) {
					return outboxEvent.failed(e.getMessage());
				}

			})
			.toList();

		outboxEventRepository.saveAll(outboxEvents);

	}

}
