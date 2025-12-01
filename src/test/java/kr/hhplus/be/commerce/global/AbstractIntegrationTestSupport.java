package kr.hhplus.be.commerce.global;

import java.util.TimeZone;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashHistoryJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.message.MessageJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.order.OrderJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.payment.PaymentJpaRepository;
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
	protected RedisTemplate<String, String> redisTemplate;

	@AfterEach
	void tearDown() {
		// Test method 실행 종료 마다, 레디스의 데이터 전체를 삭제합니다.
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
