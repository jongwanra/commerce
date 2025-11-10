package kr.hhplus.be.commerce.global;

import java.util.TimeZone;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashHistoryJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.message.MessageJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.order.OrderJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.payment.PaymentJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.product.ProductJpaRepository;
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

	@BeforeEach
	void setUp() {
		setUpTimeZone();
	}

	private void setUpTimeZone() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
}
