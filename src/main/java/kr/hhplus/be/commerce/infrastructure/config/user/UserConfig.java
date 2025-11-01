package kr.hhplus.be.commerce.infrastructure.config.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import kr.hhplus.be.commerce.domain.user.repository.UserRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.user.UserJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.user.UserRepositoryImpl;

@Configuration
public class UserConfig {
	@Bean
	public UserRepository userRepository(UserJpaRepository userJpaRepository) {
		return new UserRepositoryImpl(userJpaRepository);
	}
}
