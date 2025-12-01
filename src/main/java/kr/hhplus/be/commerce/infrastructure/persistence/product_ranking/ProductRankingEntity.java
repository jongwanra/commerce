package kr.hhplus.be.commerce.infrastructure.persistence.product_ranking;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.hhplus.be.commerce.domain.product_ranking.model.ProductRanking;
import kr.hhplus.be.commerce.infrastructure.persistence.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_ranking")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ProductRankingEntity extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDate rankingDate;

	private Long productId;
	private Integer salesCount;

	public ProductRanking toDomain() {
		return ProductRanking.restore(id, productId, salesCount, rankingDate);
	}

	public static ProductRankingEntity fromDomain(ProductRanking productRanking) {
		return ProductRankingEntity.builder()
			.id(productRanking.id())
			.productId(productRanking.productId())
			.salesCount(productRanking.salesCount())
			.rankingDate(productRanking.rankingDate())
			.build();
	}
}

