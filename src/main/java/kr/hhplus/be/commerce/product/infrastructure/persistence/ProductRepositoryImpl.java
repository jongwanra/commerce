package kr.hhplus.be.commerce.product.infrastructure.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.hhplus.be.commerce.product.domain.model.Product;
import kr.hhplus.be.commerce.product.domain.repositorty.ProductRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {
	private final ProductJpaRepository productJpaRepository;

	@Override
	public List<Product> findAllByIdInWithLock(List<Long> productIds) {
		return productJpaRepository.findAllByIdInWithLock(productIds)
			.stream()
			.map(ProductEntity::toDomain)
			.toList();
	}

	@Override
	public List<Product> saveAll(List<Product> products) {
		List<ProductEntity> entities = products.stream()
			.map(ProductEntity::fromDomain)
			.toList();

		return productJpaRepository.saveAll(entities)
			.stream()
			.map(ProductEntity::toDomain)
			.toList();
	}

}
