package kr.hhplus.be.commerce.infrastructure.persistence.product;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

@Component
public class ProductRankingKeyGenerator {
	private static final String DAILY_RANKING_KEY_FORMAT = "product_ranking:daily:%s";

	public String generate(LocalDate rankingDate) {
		final String formattedRankingDate = rankingDate.format(DateTimeFormatter.ofPattern("yyMMdd"));
		return String.format(DAILY_RANKING_KEY_FORMAT, formattedRankingDate);
	}
}
