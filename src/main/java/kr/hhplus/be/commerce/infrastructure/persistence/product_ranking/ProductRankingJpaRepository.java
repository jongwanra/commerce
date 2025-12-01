package kr.hhplus.be.commerce.infrastructure.persistence.product_ranking;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRankingJpaRepository extends JpaRepository<ProductRankingEntity, Long> {

	List<ProductRankingEntity> findAllByRankingDate(LocalDate rankingDate);
}
