package kr.hhplus.be.commerce.infrastructure.persistence.message;

import java.util.List;

import kr.hhplus.be.commerce.domain.message.enums.MessageStatus;
import kr.hhplus.be.commerce.domain.message.model.Message;
import kr.hhplus.be.commerce.domain.message.repository.MessageRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.message.entity.MessageEntity;

public class MessageRepositoryImpl implements MessageRepository {
	private final MessageJpaRepository messageJpaRepository;

	public MessageRepositoryImpl(MessageJpaRepository messageJpaRepository) {
		this.messageJpaRepository = messageJpaRepository;
	}

	@Override
	public Message save(Message message) {
		return messageJpaRepository.save(MessageEntity.fromDomain(message))
			.toDomain();
	}

	@Override
	public List<Message> findAllByStatusInOrderByCreatedAtAscLimit(List<MessageStatus> statuses, int limit) {
		return messageJpaRepository.findAllByStatusInOrderByCreatedAtAscLimit(statuses, limit)
			.stream()
			.map(MessageEntity::toDomain)
			.toList();
	}

}
