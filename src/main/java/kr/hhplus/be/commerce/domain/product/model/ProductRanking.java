package kr.hhplus.be.commerce.domain.product.model;

import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.Builder;

/**
 * ProductRanking은 기준일(rankingDate) 마다 상품의 판매량을 집계합니다.
 */
@Builder(access = AccessLevel.PRIVATE)
public record ProductRanking(
	// Long id,
	Long productId,

	// 기준일
	LocalDate rankingDate,

	// 상품 판매량
	Integer salesCount
) {

}
