package kr.hhplus.be.commerce.infrastructure.persistence.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.commerce.infrastructure.persistence.user.entity.UserEntity;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT u FROM UserEntity u WHERE u.id = :id")
	Optional<UserEntity> findByIdForUpdate(@Param("id") Long id);

}
