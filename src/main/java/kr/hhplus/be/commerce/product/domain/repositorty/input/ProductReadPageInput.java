package kr.hhplus.be.commerce.product.domain.repositorty.input;

import kr.hhplus.be.commerce.product.domain.repositorty.input.enums.ProductSortType;
import lombok.Builder;

@Builder
public record ProductReadPageInput(
	long lastId,
	int size,
	ProductSortType sortType
) {
}
