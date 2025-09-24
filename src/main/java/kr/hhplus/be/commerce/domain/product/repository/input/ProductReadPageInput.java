package kr.hhplus.be.commerce.domain.product.repository.input;

import kr.hhplus.be.commerce.domain.product.repository.input.enums.ProductSortType;
import lombok.Builder;

@Builder
public record ProductReadPageInput(
	long lastId,
	int size,
	ProductSortType sortType
) {
}
