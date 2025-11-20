package kr.hhplus.be.commerce.domain.product_ranking.model;

import java.time.LocalDate;

import kr.hhplus.be.commerce.domain.global.annotation.InfrastructureOnly;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record ProductRanking(
	Long id,
	Long productId,
	Integer salesCount,
	LocalDate rankingDate

) {

	@InfrastructureOnly
	public static ProductRanking restore(Long id, Long productId, Integer salesCount, LocalDate rankingDate) {
		return ProductRanking.builder()
			.id(id)
			.productId(productId)
			.salesCount(salesCount)
			.rankingDate(rankingDate)
			.build();
	}

	public static ProductRanking empty(Long productId) {
		return ProductRanking.builder()
			.productId(productId)
			.salesCount(0)
			.rankingDate(LocalDate.now())
			.build();
	}

	public ProductRanking renewSalesCount(Integer salesCount) {
		if (salesCount < 0) {
			throw new CommerceException("상품 판매량이 음수일 수 없습니다.");
		}
		return ProductRanking.builder()
			.id(this.id)
			.productId(this.productId)
			.salesCount(salesCount)
			.rankingDate(this.rankingDate)
			.build();
	}
}
