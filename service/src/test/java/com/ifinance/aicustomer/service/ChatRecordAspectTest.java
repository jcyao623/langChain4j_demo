package com.ifinance.aicustomer.service;

import com.ifinance.aicustomer.common.enums.ChatRole;
import com.ifinance.aicustomer.service.annotation.ChatRecord;
import com.ifinance.aicustomer.service.aspect.ChatRecordAspect;
import com.ifinance.aicustomer.service.entity.ChatMessageEntity;
import com.ifinance.aicustomer.service.repository.ChatMessageRepository;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.service.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatRecordAspectTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ProceedingJoinPoint joinPoint;

    private ChatRecordAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new ChatRecordAspect(chatMessageRepository, "qwen-plus");
    }

    @Test
    void shouldSaveUserAndAssistantMessages() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{"session-1", "你好"});
        when(joinPoint.proceed()).thenReturn(Result.<String>builder()
                .content("欢迎咨询")
                .tokenUsage(new TokenUsage(10, 20, 30))
                .build());

        Object result = aspect.recordChat(joinPoint, mock(ChatRecord.class));

        assertInstanceOf(Result.class, result);
        ArgumentCaptor<ChatMessageEntity> captor = ArgumentCaptor.forClass(ChatMessageEntity.class);
        verify(chatMessageRepository, times(2)).save(captor.capture());

        List<ChatMessageEntity> saved = captor.getAllValues();
        assertEquals(ChatRole.USER, saved.get(0).getRole());
        assertEquals("你好", saved.get(0).getContent());
        assertEquals(ChatRole.ASSISTANT, saved.get(1).getRole());
        assertEquals("欢迎咨询", saved.get(1).getContent());
        assertEquals(Integer.valueOf(10), saved.get(1).getPromptTokens());
        assertEquals(Integer.valueOf(30), saved.get(1).getTotalTokens());
    }

    @Test
    void shouldSaveUserMessageWhenAiCallFails() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{"session-1", "你好"});
        when(joinPoint.proceed()).thenThrow(new RuntimeException("ai error"));

        assertThrows(RuntimeException.class, () -> aspect.recordChat(joinPoint, mock(ChatRecord.class)));

        ArgumentCaptor<ChatMessageEntity> captor = ArgumentCaptor.forClass(ChatMessageEntity.class);
        verify(chatMessageRepository, times(1)).save(captor.capture());
        assertEquals(ChatRole.USER, captor.getValue().getRole());
    }
}
