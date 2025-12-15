package kr.hhplus.be.commerce.infrastructure.persistence.processed_message;

import org.springframework.stereotype.Repository;

import kr.hhplus.be.commerce.domain.processed_message.model.ProcessedMessage;
import kr.hhplus.be.commerce.domain.processed_message.repository.ProcessedMessageRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.processed_message.entity.ProcessedMessageEntity;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProcessedMessageRepositoryImpl implements ProcessedMessageRepository {
	private final ProcessedMessageJpaRepository processedMessageJpaRepository;

	@Override
	public ProcessedMessage saveAndFlush(ProcessedMessage processedMessage) {
		return processedMessageJpaRepository.saveAndFlush(ProcessedMessageEntity.fromDomain(processedMessage))
			.toDomain();
	}

	@Override
	public boolean existsByMessageId(String messageId) {
		return existsByMessageId(messageId);
	}
}
