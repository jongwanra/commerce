package kr.hhplus.be.commerce.infrastructure.persistence.product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.commerce.infrastructure.persistence.product.entity.ProductEntity;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT p FROM ProductEntity p WHERE p.id IN :productIds")
	List<ProductEntity> findAllByIdInWithLock(@Param("productIds") List<Long> productIds);

	Optional<ProductEntity> findByName(String name);
}
