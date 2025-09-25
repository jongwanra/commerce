package kr.hhplus.be.commerce.global;

import java.util.TimeZone;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTestSupport {

	@BeforeEach
	void setUp() {
		setUpTimeZone();
	}

	private void setUpTimeZone() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
}
