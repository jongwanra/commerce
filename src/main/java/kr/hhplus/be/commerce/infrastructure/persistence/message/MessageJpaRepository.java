package kr.hhplus.be.commerce.infrastructure.persistence.message;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.hhplus.be.commerce.domain.message.enums.MessageStatus;
import kr.hhplus.be.commerce.infrastructure.persistence.message.entity.MessageEntity;

public interface MessageJpaRepository extends JpaRepository<MessageEntity, Long> {
	@Query("SELECT oe FROM MessageEntity oe WHERE oe.status IN (:statuses) ORDER BY oe.createdAt ASC LIMIT :limit")
	List<MessageEntity> findAllByStatusInOrderByCreatedAtAscLimit(@Param("statuses") List<MessageStatus> statuses,
		@Param("limit") int limit);
}
