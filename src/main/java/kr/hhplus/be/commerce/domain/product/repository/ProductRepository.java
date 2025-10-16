package kr.hhplus.be.commerce.domain.product.repository;

import java.util.List;

import kr.hhplus.be.commerce.domain.product.model.Product;

public interface ProductRepository {
	List<Product> findAllByIdInForUpdate(List<Long> productIds);

	List<Product> saveAll(List<Product> products);
	
}
