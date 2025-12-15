package kr.hhplus.be.commerce.domain.processed_message.repository;

import kr.hhplus.be.commerce.domain.processed_message.model.ProcessedMessage;

public interface ProcessedMessageRepository {
	ProcessedMessage saveAndFlush(ProcessedMessage processedMessage);

	boolean existsByMessageId(String messageId);
}
