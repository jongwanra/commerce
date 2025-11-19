package kr.hhplus.be.commerce.infrastructure.persistence.product;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductRankingKeyGeneratorTest {
	private ProductRankingKeyGenerator productRankingKeyGenerator;

	@BeforeEach
	void setUp() {
		productRankingKeyGenerator = new ProductRankingKeyGenerator();
	}

	@Test
	void productRanking의_키를_발급할_수_있다() {
		// given
		final LocalDate today = LocalDate.of(2025, 11, 19);
		// when & then
		assertThat(productRankingKeyGenerator.generate(today)).isEqualTo(
			"product_ranking:daily:251119");

	}
}
