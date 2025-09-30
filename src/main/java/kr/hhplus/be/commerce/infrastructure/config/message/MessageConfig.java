package kr.hhplus.be.commerce.infrastructure.config.message;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import kr.hhplus.be.commerce.domain.message.repository.MessageRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.message.MessageJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.message.MessageRepositoryImpl;

@Configuration
public class MessageConfig {

	@Bean
	public MessageRepository outboxEventRepository(MessageJpaRepository messageJpaRepository) {
		return new MessageRepositoryImpl(messageJpaRepository);
	}

}
