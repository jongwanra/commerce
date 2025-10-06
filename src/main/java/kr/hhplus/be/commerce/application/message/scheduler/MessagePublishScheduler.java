package kr.hhplus.be.commerce.application.message.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import kr.hhplus.be.commerce.application.message.mapper.MessagePublisherMapper;
import kr.hhplus.be.commerce.application.message.publisher.MessagePublisher;
import kr.hhplus.be.commerce.domain.message.enums.MessageStatus;
import kr.hhplus.be.commerce.domain.message.model.Message;
import kr.hhplus.be.commerce.domain.message.model.message_payload.MessagePayload;
import kr.hhplus.be.commerce.domain.message.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile({"local"})
public class MessagePublishScheduler {
	private static final int BATCH_SIZE = 20;
	private static final int FIVE_SECONDS = 5 * 1000;

	private final MessageRepository messageRepository;
	private final MessagePublisherMapper messagePublisherMapper;
	private final TransactionTemplate transactionTemplate;

	@Scheduled(fixedDelay = FIVE_SECONDS)
	public void execute() {
		List<Message> messages = messageRepository.findAllByStatusInOrderByCreatedAtAscLimit(
			MessageStatus.PUBLISHABLE_STATUES, BATCH_SIZE);

		for (Message message : messages) {
			try {
				MessagePayload messagePayload = message.payload();
				MessagePublisher messagePublisher = messagePublisherMapper.get(message.type());
				messagePublisher.publish(messagePayload);
				transactionTemplate.executeWithoutResult(status -> messageRepository.save(message.published(
					LocalDateTime.now())));
			} catch (Exception e) {
				transactionTemplate.executeWithoutResult(
					status -> messageRepository.save(message.failed(e.getMessage(), LocalDateTime.now())));
				log.error("메세지 발행 중 실패 케이스가 발생했습니다. message: {} / error: {}", message, e.getMessage());
				// 순서대로 처리를 보장하기 위해, 실패한 경우에는 멈추도록 구현했습니다.
				break;
			}
		}

	}

}
