package com.ifinance.aicustomer.service;

import com.ifinance.aicustomer.common.enums.ChatRole;
import com.ifinance.aicustomer.service.dto.ChatRequest;
import com.ifinance.aicustomer.service.dto.ChatResponse;
import com.ifinance.aicustomer.service.entity.ChatMessageEntity;
import com.ifinance.aicustomer.service.repository.ChatMessageRepository;
import com.ifinance.aicustomer.service.service.impl.ChatServiceImpl;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.output.TokenUsage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.StringUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    private ChatServiceImpl chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatServiceImpl(chatModel, chatMessageRepository, "qwen-plus");
    }

    @Test
    void shouldSaveUserAndAssistantMessages() {
        when(chatMessageRepository.findBySessionIdOrderByCreatedAtAsc("session-1")).thenReturn(List.of());
        when(chatMessageRepository.save(any(ChatMessageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(chatModel.chat(anyList())).thenReturn(dev.langchain4j.model.chat.response.ChatResponse.builder()
                .aiMessage(AiMessage.from("欢迎咨询"))
                .tokenUsage(new TokenUsage(10, 20, 30))
                .modelName("qwen-plus")
                .build());

        ChatResponse response = chatService.chat(new ChatRequest("你好", "session-1"));

        assertEquals("session-1", response.sessionId());
        assertEquals("欢迎咨询", response.message());
        assertEquals("qwen-plus", response.modelName());

        ArgumentCaptor<ChatMessageEntity> captor = ArgumentCaptor.forClass(ChatMessageEntity.class);
        verify(chatMessageRepository, times(2)).save(captor.capture());
        List<ChatMessageEntity> saved = captor.getAllValues();

        assertEquals(ChatRole.USER, saved.get(0).getRole());
        assertEquals("你好", saved.get(0).getContent());
        assertEquals(ChatRole.ASSISTANT, saved.get(1).getRole());
        assertEquals("欢迎咨询", saved.get(1).getContent());
        assertEquals(Integer.valueOf(10), saved.get(1).getPromptTokens());
        assertEquals(Integer.valueOf(20), saved.get(1).getCompletionTokens());
        assertEquals(Integer.valueOf(30), saved.get(1).getTotalTokens());
    }

    @Test
    void shouldCreateSessionIdWhenRequestDoesNotProvideOne() {
        when(chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(anyString())).thenReturn(List.of());
        when(chatMessageRepository.save(any(ChatMessageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(chatModel.chat(anyList())).thenReturn(dev.langchain4j.model.chat.response.ChatResponse.builder()
                .aiMessage(AiMessage.from("ok"))
                .build());

        ChatResponse response = chatService.chat(new ChatRequest("你好", " "));

        assertTrue(StringUtils.hasText(response.sessionId()));
        assertEquals("qwen-plus", response.modelName());
    }
}
