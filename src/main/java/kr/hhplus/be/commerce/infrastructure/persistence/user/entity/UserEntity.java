package kr.hhplus.be.commerce.infrastructure.persistence.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.hhplus.be.commerce.domain.user.model.User;
import kr.hhplus.be.commerce.infrastructure.persistence.global.entity.BaseTimeEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.user.entity.enums.UserStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
public class UserEntity extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private UserStatus status;

	private String email;

	private String encryptedPassword;

	@Builder
	private UserEntity(Long id, UserStatus status, String email, String encryptedPassword) {
		this.id = id;
		this.status = status;
		this.email = email;
		this.encryptedPassword = encryptedPassword;
	}

	public User toDomain() {
		return User.restore(id, status, email, encryptedPassword);
	}
}
