package kr.hhplus.be.commerce.application.cash;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;

import kr.hhplus.be.commerce.application.order.OrderPlaceProcessor;
import kr.hhplus.be.commerce.domain.cash.model.Cash;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.domain.product.model.Product;
import kr.hhplus.be.commerce.global.AbstractIntegrationTestSupport;
import kr.hhplus.be.commerce.global.annotation.IntegrationTest;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.CashEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.CashHistoryEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.product.entity.ProductEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.user.entity.UserEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.user.entity.enums.UserStatus;

public class CashConcurrencyIntegrationTest extends AbstractIntegrationTestSupport {
	@Autowired
	private OrderPlaceProcessor orderPlaceProcessor;
	@Autowired
	private CashDeductAdminProcessor cashDeductAdminProcessor;

	/**
	 * 작성 이유: 동일 사용자의 여러 주문 요청이 동시에 발생할 때,
	 * 잔액 검증의 Race condition으로 인한 음수 잔액 발생 여부를 검증합니다.
	 */
	@IntegrationTest
	void 동일_사용자가_여러_상품을_동시에_주문할_때_잔액이_부족하면_예외를_발생시킨다() throws InterruptedException {
		// given
		final int threadCount = 2;
		final BigDecimal balance = BigDecimal.valueOf(9_900);
		UserEntity user = userJpaRepository.save(UserEntity.builder()
			.email("userA@gmail.com")
			.encryptedPassword("encrypted_password")
			.status(UserStatus.ACTIVE)
			.build());

		final Long userId = user.getId();
		cashJpaRepository.save(
			CashEntity.fromDomain(Cash.restore(
				null,
				userId,
				balance,
				0L
			)));

		Product productA = productJpaRepository.save(ProductEntity.builder()
			.name("오뚜기 진라면 매운맛 120g")
			.price(BigDecimal.valueOf(5_000))
			.stock(1)
			.build()).toDomain();

		Product productB = productJpaRepository.save(ProductEntity.builder()
			.name("스타벅스 아메리카노")
			.price(BigDecimal.valueOf(5_000))
			.stock(1)
			.build()).toDomain();

		List<Product> products = List.of(productA, productB);

		CountDownLatch countDownLatch = new CountDownLatch(threadCount);
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

		// when
		IntStream.range(0, threadCount)
			.forEach((index) -> executorService.execute(() -> {
				try {
					orderPlaceProcessor.execute(generateCommand(userId, products.get(index)));
				} catch (Exception e) {
				} finally {
					countDownLatch.countDown();
				}
			}));

		countDownLatch.await();

		// then
		CashEntity cash = cashJpaRepository.findByUserId(userId)
			.orElseThrow();

		assertThat(cash.getBalance().compareTo(BigDecimal.valueOf(4_900))).isZero()
			.as("처음 주문한 상품만 결제가 가능해야 한다.");
	}

	/**
	 * 작성 이유: 공유 자원인 Cash에 서로 다른 요청인 사용자 주문과 어드민 잔액 차감 요청이 동시에 들어왔을 때
	 * 잔액이 음수가 되는지 검증하기 위해 작성했습니다.
	 *
	 * 사용자 보유 잔액: 9,900원
	 * 주문 상품 가격: 5,000원
	 * 어드민 잔액 차감 금액: 5,000원
	 * 예상 결과: 4,900원이 남고 하나의 요청은 예외 발생시킴.
	 */
	@IntegrationTest
	void 사용자_주문과_관리자의_잔액_차감이_동시에_발생했을_때_잔액이_부족할_경우_예외를_발생시킨다() throws InterruptedException {
		// given
		final int threadCount = 2;
		final BigDecimal balance = BigDecimal.valueOf(9_900);
		final BigDecimal productPrice = BigDecimal.valueOf(5_000);
		final BigDecimal deductionBalance = BigDecimal.valueOf(5_000);
		UserEntity user = userJpaRepository.save(UserEntity.builder()
			.email("userA@gmail.com")
			.encryptedPassword("encrypted_password")
			.status(UserStatus.ACTIVE)
			.build());

		final Long userId = user.getId();
		cashJpaRepository.save(
			CashEntity.fromDomain(Cash.restore(
				null,
				userId,
				balance,
				0L
			)));

		Product product = productJpaRepository.save(ProductEntity.builder()
			.name("오뚜기 진라면 매운맛 120g")
			.price(productPrice)
			.stock(1)
			.build()).toDomain();

		CountDownLatch countDownLatch = new CountDownLatch(threadCount);
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger failureCount = new AtomicInteger(0);
		List<Exception> exceptions = new ArrayList<>();

		// when
		// 사용자가 상품을 주문합니다.
		executorService.execute(() -> {
			try {
				orderPlaceProcessor.execute(generateCommand(userId, product));
				successCount.incrementAndGet();
			} catch (Exception e) {
				exceptions.add(e);
				failureCount.incrementAndGet();
			} finally {
				countDownLatch.countDown();
			}
		});

		// 어드민이 사용자의 잔액을 차감합니다.
		executorService.execute(() -> {
			try {
				cashDeductAdminProcessor.execute(new CashDeductAdminProcessor.Command(userId, deductionBalance));
				successCount.incrementAndGet();
			} catch (Exception e) {
				exceptions.add(e);
				failureCount.incrementAndGet();
			} finally {
				countDownLatch.countDown();
			}
		});

		countDownLatch.await();

		// then
		CashEntity cash = cashJpaRepository.findByUserId(userId)
			.orElseThrow();
		List<CashHistoryEntity> cashHistories = cashHistoryJpaRepository.findAllByUserId(userId);

		assertThat(successCount.get()).isEqualTo(1)
			.as("사용자 주문 혹은 어드민 잔액 차감 둘 중에 하나만 성공해야 한다.");
		assertThat(failureCount.get()).isEqualTo(1)
			.as("사용자 주문 혹은 어드민 잔액 차감 둘 중에 하나만 실패해야 한다.");
		assertThat(exceptions.size()).isEqualTo(1);
		assertThat(exceptions.get(0)).isInstanceOf(CommerceException.class)
			.hasMessage("잔액이 부족합니다. 잔액을 충전해주세요.");

		assertThat(cash.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(4_900))
			.as("사용자 주문 혹은 어드민 잔액 차감 중에 하나만 성공하여 잔액은 4,900원이 남아야 한다.");
		assertThat(cashHistories.size()).isEqualTo(1);
	}

	private OrderPlaceProcessor.Command generateCommand(Long userId, Product product) {
		final String idempotencyKey = UUID.randomUUID().toString();
		final BigDecimal paymentAmount = product.price();
		final LocalDateTime now = LocalDateTime.now();
		List<OrderPlaceProcessor.OrderLineCommand> orderLineCommands = List.of(
			new OrderPlaceProcessor.OrderLineCommand(product.id(), 1));

		return new OrderPlaceProcessor.Command(idempotencyKey, userId, null, paymentAmount, now, orderLineCommands);
	}
}
