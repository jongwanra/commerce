package kr.hhplus.be.commerce.infrastructure.persistence.product;

import static kr.hhplus.be.commerce.infrastructure.persistence.product.entity.QProductEntity.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import kr.hhplus.be.commerce.domain.product.model.ProductDetailView;
import kr.hhplus.be.commerce.domain.product.model.ProductSummaryView;
import kr.hhplus.be.commerce.domain.product.repository.ProductReader;
import kr.hhplus.be.commerce.domain.product.repository.input.ProductReadPageInput;
import kr.hhplus.be.commerce.domain.product.repository.input.enums.ProductSortType;
import kr.hhplus.be.commerce.presentation.global.response.CursorPage;

@Repository
public class ProductReaderImpl implements ProductReader {
	private JPAQueryFactory queryFactory;

	public ProductReaderImpl(EntityManager entityManager) {
		this.queryFactory = new JPAQueryFactory(entityManager);
	}

	@Override
	public CursorPage<ProductSummaryView> readPage(ProductReadPageInput input) {

		List<ProductSummaryView> products = queryFactory
			.select(constructorOfProductSummaryView())
			.from(productEntity)
			.where(buildWhereCondition(input))
			.orderBy(buildOrderBy(input))
			.limit(input.size() + 1)
			.fetch();

		final boolean hasNext = products.size() > input.size();
		if (hasNext) {
			products.remove(input.size());
		}

		final long totalCount = queryFactory
			.select(productEntity.id.count())
			.from(productEntity)
			.fetchFirst();

		return CursorPage.of(
			hasNext,
			totalCount,
			products
		);
	}

	@Override
	public Optional<ProductDetailView> readById(Long productId) {
		return Optional.ofNullable(
			queryFactory
				.select(constructorOfProductDetailView())
				.from(productEntity)
				.where(productEntity.id.eq(productId))
				.fetchFirst()
		);
	}

	private BooleanExpression buildWhereCondition(ProductReadPageInput input) {
		return switch (input.sortType()) {
			case OLDEST -> buildWhereConditionForOldest(input);
			case NEWEST -> buildWhereConditionForNewest(input);
		};
	}

	private OrderSpecifier<?> buildOrderBy(ProductReadPageInput input) {
		// oldest
		if (input.sortType().equals(ProductSortType.OLDEST)) {
			return productEntity.createdAt.asc();
		}
		// newest
		return productEntity.createdAt.desc();
	}

	private BooleanExpression buildWhereConditionForOldest(ProductReadPageInput input) {
		if (input.lastId() == 0) {
			return null;
		}
		final LocalDateTime lastCreatedAt = queryFactory
			.select(productEntity.createdAt)
			.from(productEntity)
			.where(productEntity.id.eq(input.lastId()))
			.fetchFirst();

		return productEntity.createdAt.gt(lastCreatedAt);
	}

	private BooleanExpression buildWhereConditionForNewest(ProductReadPageInput input) {
		if (input.lastId() == 0) {
			return null;
		}
		final LocalDateTime lastCreatedAt = queryFactory
			.select(productEntity.createdAt)
			.from(productEntity)
			.where(productEntity.id.eq(input.lastId()))
			.fetchFirst();

		return productEntity.createdAt.lt(lastCreatedAt);
	}

	private ConstructorExpression<ProductSummaryView> constructorOfProductSummaryView() {
		return Projections.constructor(
			ProductSummaryView.class,
			productEntity.id,
			productEntity.name,
			productEntity.price,
			productEntity.createdAt
		);
	}

	private ConstructorExpression<ProductDetailView> constructorOfProductDetailView() {
		return Projections.constructor(
			ProductDetailView.class,
			productEntity.id,
			productEntity.name,
			productEntity.price,
			productEntity.stock,
			productEntity.createdAt
		);
	}
}
