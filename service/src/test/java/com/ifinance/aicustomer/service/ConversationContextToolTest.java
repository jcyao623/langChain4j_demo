package com.ifinance.aicustomer.service;

import com.ifinance.aicustomer.common.enums.ChatRole;
import com.ifinance.aicustomer.service.entity.ChatMessageEntity;
import com.ifinance.aicustomer.service.repository.ChatMessageRepository;
import com.ifinance.aicustomer.service.tool.ConversationContextTool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationContextToolTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Test
    void shouldFormatHistoryForModel() {
        when(chatMessageRepository.findBySessionIdOrderByCreatedAtAsc("session-1"))
                .thenReturn(List.of(entity(ChatRole.USER, "你好"), entity(ChatRole.ASSISTANT, "欢迎咨询")));

        String history = new ConversationContextTool(chatMessageRepository).getChatHistory("session-1");

        assertTrue(history.contains("USER：你好"));
        assertTrue(history.contains("ASSISTANT：欢迎咨询"));
    }

    private ChatMessageEntity entity(ChatRole role, String content) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setSessionId("session-1");
        entity.setRole(role);
        entity.setContent(content);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }
}
