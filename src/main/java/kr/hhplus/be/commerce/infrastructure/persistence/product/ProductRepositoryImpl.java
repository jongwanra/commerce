package kr.hhplus.be.commerce.infrastructure.persistence.product;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import kr.hhplus.be.commerce.domain.product.model.Product;
import kr.hhplus.be.commerce.domain.product.repository.ProductRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.product.entity.ProductEntity;
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

	@Override
	public Optional<Product> findByName(String name) {
		return productJpaRepository.findByName(name)
			.map(ProductEntity::toDomain);
	}

}
