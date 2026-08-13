package com.ifinance.aicustomer.service;

import com.ifinance.aicustomer.service.assistant.AiChatGateway;
import com.ifinance.aicustomer.service.dto.ChatRequest;
import com.ifinance.aicustomer.service.dto.ChatResponse;
import com.ifinance.aicustomer.service.mapper.ChatMessageMapper;
import com.ifinance.aicustomer.service.service.impl.ChatServiceImpl;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.service.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.StringUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private AiChatGateway aiChatGateway;

    @Mock
    private ChatMessageMapper chatMessageMapper;

    private ChatServiceImpl chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatServiceImpl(aiChatGateway, chatMessageMapper, "qwen-plus");
    }

    @Test
    void shouldReturnAssistantReply() {
        when(aiChatGateway.chat(eq("session-1"), eq("你好"), anyString())).thenReturn(Result.<String>builder()
                .content("欢迎咨询")
                .tokenUsage(new TokenUsage(10, 20, 30))
                .build());

        ChatResponse response = chatService.chat(new ChatRequest("你好", "session-1"));

        assertEquals("session-1", response.sessionId());
        assertEquals("欢迎咨询", response.message());
        assertEquals("qwen-plus", response.modelName());
        verifyNoInteractions(chatMessageMapper);
    }

    @Test
    void shouldCreateSessionIdWhenRequestDoesNotProvideOne() {
        when(aiChatGateway.chat(anyString(), eq("你好"), anyString())).thenReturn(Result.<String>builder()
                .content("ok")
                .build());

        ChatResponse response = chatService.chat(new ChatRequest("你好", " "));

        assertTrue(StringUtils.hasText(response.sessionId()));
        assertEquals("qwen-plus", response.modelName());
    }
}
