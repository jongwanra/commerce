package kr.hhplus.be.commerce.domain.processed_message.repository;

import kr.hhplus.be.commerce.domain.processed_message.model.ProcessedMessage;

public interface ProcessedMessageRepository {

	boolean existsByMessageId(String messageId);

	ProcessedMessage save(ProcessedMessage processedMessage);
}
