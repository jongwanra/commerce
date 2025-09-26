package kr.hhplus.be.commerce.global;

import java.util.TimeZone;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.product.ProductJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.user.UserJpaRepository;

@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTestSupport {
	@Autowired
	protected UserJpaRepository userJpaRepository;
	@Autowired
	protected ProductJpaRepository productJpaRepository;
	@Autowired
	protected CashJpaRepository cashJpaRepository;

	@BeforeEach
	void setUp() {
		setUpTimeZone();
	}

	private void setUpTimeZone() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
}
