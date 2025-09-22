package kr.hhplus.be.commerce.product.domain.repositorty;

import java.util.List;

import kr.hhplus.be.commerce.product.domain.model.Product;

public interface ProductRepository {
	List<Product> findAllByIdInWithLock(List<Long> productIds);

	List<Product> saveAll(List<Product> products);
}
