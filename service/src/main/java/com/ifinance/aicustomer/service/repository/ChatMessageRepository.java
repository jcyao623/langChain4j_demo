package com.ifinance.aicustomer.service.repository;

import com.ifinance.aicustomer.service.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 聊天消息数据访问接口。
 */
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    /**
     * 按创建时间正序查询会话消息。
     */
    List<ChatMessageEntity> findBySessionIdOrderByCreatedAtAsc(String sessionId);
}
