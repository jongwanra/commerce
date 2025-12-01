package kr.hhplus.be.commerce.infrastructure.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.hhplus.be.commerce.infrastructure.persistence.user.entity.UserEntity;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

}
