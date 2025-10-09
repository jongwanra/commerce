package kr.hhplus.be.commerce.infrastructure.persistence.cash.entity;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.hhplus.be.commerce.domain.cash.model.Cash;
import kr.hhplus.be.commerce.infrastructure.persistence.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "cash")
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class CashEntity extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;

	private BigDecimal balance;

	public static CashEntity fromDomain(Cash cash) {
		return CashEntity.builder()
			.id(cash.id())
			.userId(cash.userId())
			.balance(cash.balance())
			.build();
	}

	public Cash toDomain() {
		return Cash.restore(
			id,
			userId,
			balance
		);
	}
}
