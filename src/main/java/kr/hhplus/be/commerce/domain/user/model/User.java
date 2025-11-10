package kr.hhplus.be.commerce.domain.user.model;

import kr.hhplus.be.commerce.domain.global.annotation.InfrastructureOnly;
import kr.hhplus.be.commerce.infrastructure.persistence.user.entity.enums.UserStatus;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record User(
	Long id,
	UserStatus status,
	String email,
	String encryptedPassword
) {

	@InfrastructureOnly
	public static User restore(Long id, UserStatus status, String email, String encryptedPassword) {
		return User.builder()
			.id(id)
			.status(status)
			.email(email)
			.encryptedPassword(encryptedPassword)
			.build();
	}
}
