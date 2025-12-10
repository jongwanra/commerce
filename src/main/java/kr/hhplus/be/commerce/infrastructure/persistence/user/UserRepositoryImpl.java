package kr.hhplus.be.commerce.infrastructure.persistence.user;

import java.util.Optional;

import kr.hhplus.be.commerce.domain.user.model.User;
import kr.hhplus.be.commerce.domain.user.repository.UserRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
	private final UserJpaRepository userJpaRepository;

	@Override
	public Optional<User> findById(Long id) {
		return userJpaRepository.findById(id)
			.map(UserEntity::toDomain);
	}
}
