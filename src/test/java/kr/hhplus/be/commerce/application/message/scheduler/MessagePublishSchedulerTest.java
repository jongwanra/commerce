package kr.hhplus.be.commerce.application.message.scheduler;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import kr.hhplus.be.commerce.domain.message.enums.MessageStatus;
import kr.hhplus.be.commerce.domain.message.enums.MessageTargetType;
import kr.hhplus.be.commerce.domain.message.mapper.MessagePublisherMapper;
import kr.hhplus.be.commerce.domain.message.model.Message;
import kr.hhplus.be.commerce.domain.message.model.message_payload.OrderConfirmedMessagePayload;
import kr.hhplus.be.commerce.domain.message.repository.MessageRepository;
import kr.hhplus.be.commerce.global.AbstractIntegrationTestSupport;
import kr.hhplus.be.commerce.global.annotation.IntegrationTest;
import kr.hhplus.be.commerce.infrastructure.client.slack.SlackException;
import kr.hhplus.be.commerce.infrastructure.client.slack.SlackSendMessageClient;
import kr.hhplus.be.commerce.infrastructure.persistence.message.entity.MessageEntity;

class MessagePublishSchedulerTest extends AbstractIntegrationTestSupport {
	private MessagePublishScheduler messagePublishScheduler;

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private MessagePublisherMapper messagePublisherMapper;

	@MockitoBean
	private SlackSendMessageClient slackSendMessageClient;

	@BeforeEach
	void setUp() {
		messagePublishScheduler = new MessagePublishScheduler(
			messageRepository,
			messagePublisherMapper,
			transactionTemplate
		);
	}

	/**
	 * 작성 이유: 주문 확정 이후, Slack(외부 API)에 데이터 전송이 실패했을 경우, Fallback 처리를 검증하기 위해 작성했습니다.
	 * 저장된 Slack(외부 API)에 데이터 전송을 최대 3회까지 재시도합니다. 3회 재시도 이후에도 실패한다면
	 * Message에 대한 운영팀의 수동 처리가 필요합니다(MessageStatus: DEAD_LETTER)
	 */
	@IntegrationTest
	void Slack_전송_실패시_최대_3회까지_재시도_한다() {
		// given
		Long orderId = 3232L;
		messageRepository.save(Message.ofPending(
			orderId,
			MessageTargetType.ORDER,
			OrderConfirmedMessagePayload.from(3232L))
		);

		// mock
		doThrow(new SlackException("슬랙에 메세지를 전송하는데 실패했습니다."))
			.when(slackSendMessageClient)
			.send(anyString());

		// when & then

		// 1, 2번째 재시도
		for (int retryCount = 1; retryCount <= 2; retryCount++) {
			messagePublishScheduler.execute();

			List<MessageEntity> messageEntities = messageJpaRepository.findAll();
			assertThat(messageEntities).hasSize(1);

			MessageEntity messageEntity = messageEntities.get(0);
			assertThat(messageEntity.getStatus()).isEqualTo(MessageStatus.FAILED);
			assertThat(messageEntity.getTargetId()).isEqualTo(3232L);
			assertThat(messageEntity.getFailedAt()).isNotNull();
			assertThat(messageEntity.getFailedReason()).isEqualTo("슬랙에 메세지를 전송하는데 실패했습니다.");
			assertThat(messageEntity.getFailedCount()).isEqualTo(retryCount);
		}

		// 3번째 재시도
		messagePublishScheduler.execute();
		List<MessageEntity> messageEntities = messageJpaRepository.findAll();
		assertThat(messageEntities).hasSize(1);

		MessageEntity messageEntity = messageEntities.get(0);
		assertThat(messageEntity.getStatus()).isEqualTo(MessageStatus.DEAD_LETTER);
		assertThat(messageEntity.getTargetId()).isEqualTo(3232L);
		assertThat(messageEntity.getFailedAt()).isNotNull();
		assertThat(messageEntity.getFailedReason()).isEqualTo("슬랙에 메세지를 전송하는데 실패했습니다.");
		assertThat(messageEntity.getFailedCount()).isEqualTo(3);

	}
}


