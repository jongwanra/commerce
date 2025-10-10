package kr.hhplus.be.commerce.domain.message.repository;

import java.util.List;

import kr.hhplus.be.commerce.domain.message.enums.MessageStatus;
import kr.hhplus.be.commerce.domain.message.model.Message;

public interface MessageRepository {
	Message save(Message message);

	List<Message> findAllByStatusInOrderByCreatedAtAscLimit(List<MessageStatus> statuses, int limit);
	
}
