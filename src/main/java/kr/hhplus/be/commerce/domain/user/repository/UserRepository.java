package kr.hhplus.be.commerce.domain.user.repository;

import java.util.Optional;

import kr.hhplus.be.commerce.domain.user.model.User;

public interface UserRepository {
	Optional<User> findByIdForUpdate(Long id);
}
