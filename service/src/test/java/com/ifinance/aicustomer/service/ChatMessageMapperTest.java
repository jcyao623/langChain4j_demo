package com.ifinance.aicustomer.service;

import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import com.ifinance.aicustomer.common.enums.ChatRole;
import com.ifinance.aicustomer.service.entity.ChatMessageEntity;
import com.ifinance.aicustomer.service.mapper.ChatMessageMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MybatisPlusTest
class ChatMessageMapperTest {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Test
    void shouldFindMessagesOrderedByCreatedAt() {
        LocalDateTime base = LocalDateTime.of(2026, 8, 12, 10, 0);
        chatMessageMapper.insert(entity("s1", ChatRole.USER, "第一条", base));
        chatMessageMapper.insert(entity("s1", ChatRole.ASSISTANT, "第二条", base.plusMinutes(1)));
        chatMessageMapper.insert(entity("s2", ChatRole.USER, "其他会话", base));

        List<ChatMessageEntity> result = chatMessageMapper.findBySessionIdOrderByCreatedAtAsc("s1");

        assertEquals(2, result.size());
        assertEquals("第一条", result.get(0).getContent());
        assertEquals("第二条", result.get(1).getContent());
    }

    private ChatMessageEntity entity(String sessionId, ChatRole role, String content, LocalDateTime createdAt) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setSessionId(sessionId);
        entity.setRole(role);
        entity.setContent(content);
        entity.setModelName("qwen-plus");
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(createdAt);
        return entity;
    }
}
