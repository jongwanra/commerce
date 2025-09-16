package kr.hhplus.be.commerce.user.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.hhplus.be.commerce.user.persistence.entity.UserEntity;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
}
