package kr.hhplus.be.commerce.cash.application;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import kr.hhplus.be.commerce.global.AbstractIntegrationTestSupport;
import kr.hhplus.be.commerce.global.annotations.IntegrationTest;
import kr.hhplus.be.commerce.user.persistence.UserEntity;
import kr.hhplus.be.commerce.user.persistence.UserJpaRepository;

public class CashChargeProcessorIntegrationTest extends AbstractIntegrationTestSupport {
	@Autowired
	private UserJpaRepository userJpaRepository;

	@IntegrationTest
	void 충전할_수_있다() {
		List<UserEntity> users = userJpaRepository.findAll();
		for (UserEntity user : users) {
			System.out.println(user.getEmail());
		}
		System.out.println("충전할_수_있다");
	}
}
