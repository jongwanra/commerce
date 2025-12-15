package kr.hhplus.be.commerce.global;

import static org.mockito.Mockito.*;

import java.util.TimeZone;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import kr.hhplus.be.commerce.infrastructure.client.slack.SlackSendMessageClient;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashHistoryJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.message.MessageJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.order.OrderJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.payment.PaymentJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.processed_message.ProcessedMessageJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.product.ProductJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.product_ranking.ProductRankingJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.user.UserJpaRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTestSupport {
	@Autowired
	protected UserJpaRepository userJpaRepository;
	@Autowired
	protected ProductJpaRepository productJpaRepository;
	@Autowired
	protected CashJpaRepository cashJpaRepository;

	@Autowired
	protected CashHistoryJpaRepository cashHistoryJpaRepository;
	@Autowired
	protected MessageJpaRepository messageJpaRepository;

	@Autowired
	protected PaymentJpaRepository paymentJpaRepository;

	@Autowired
	protected OrderJpaRepository orderJpaRepository;

	@Autowired
	protected ProductRankingJpaRepository productRankingJpaRepository;

	@Autowired
	protected ProcessedMessageJpaRepository processedMessageJpaRepository;

	@Autowired
	protected RedisTemplate<String, String> redisTemplate;

	@Autowired
	protected KafkaTemplate<String, String> kafkaTemplate;

	@MockitoBean
	protected SlackSendMessageClient slackSendMessageClient;

	@AfterEach
	void tearDown() {
		flushRedisDatabase();
		resetMocks();

	}

	/**
	 * 스프링 통합 테스트 환경에서 외부 시스템은 MockBean으로 대체합니다.
	 * 각 통합테스트 마다 MockBean이 공유되기 때문에 초기화 작업이 필요합니다.
	 */
	private void resetMocks() {
		reset(slackSendMessageClient);
	}

	private void flushRedisDatabase() {
		redisTemplate.getConnectionFactory()
			.getConnection()
			.serverCommands()
			.flushDb();
	}

	static {
		setUpTimeZone();
	}

	private static void setUpTimeZone() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
}
