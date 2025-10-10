package kr.hhplus.be.commerce.domain.product.repository;

import java.util.List;
import java.util.Optional;

import kr.hhplus.be.commerce.domain.product.model.Product;

public interface ProductRepository {
	List<Product> findAllByIdInWithLock(List<Long> productIds);

	List<Product> saveAll(List<Product> products);
	
	Optional<Product> findByName(String name);
}
