package com.ifinance.aicustomer.service.aspect;

import com.ifinance.aicustomer.common.enums.ChatRole;
import com.ifinance.aicustomer.service.annotation.ChatRecord;
import com.ifinance.aicustomer.service.entity.ChatMessageEntity;
import com.ifinance.aicustomer.service.repository.ChatMessageRepository;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.service.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 对话消息登记切面：统一将用户消息与 AI 回复写入数据库。
 */
@Aspect
@Component
public class ChatRecordAspect {

    private static final Logger log = LoggerFactory.getLogger(ChatRecordAspect.class);

    private final ChatMessageRepository chatMessageRepository;
    private final String defaultModelName;

    public ChatRecordAspect(ChatMessageRepository chatMessageRepository,
                            @Value("${openai-compatible.aliyun.model:qwen-plus}") String defaultModelName) {
        this.chatMessageRepository = chatMessageRepository;
        this.defaultModelName = defaultModelName;
    }

    @Around("@annotation(chatRecord)")
    /**
     * 在 AI Service 调用前后登记用户消息与 AI 回复。
     */
    public Object recordChat(ProceedingJoinPoint joinPoint, ChatRecord chatRecord) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String sessionId = (String) args[0];
        String userMessage = (String) args[1];
        LocalDateTime now = LocalDateTime.now();

        try {
            Object result = joinPoint.proceed();
            if (result instanceof Result<?> serviceResult && serviceResult.content() instanceof String aiMessage) {
                TokenUsage tokenUsage = serviceResult.tokenUsage();
                String modelName = resolveModelName(serviceResult);
                log.info("登记用户消息, sessionId={}, messageLength={}", sessionId, userMessage.length());
                chatMessageRepository.save(toEntity(sessionId, ChatRole.USER, userMessage, null, null, now));
                log.info("登记AI回复, sessionId={}, replyLength={}, modelName={}",
                        sessionId, aiMessage.length(), modelName);
                chatMessageRepository.save(
                        toEntity(sessionId, ChatRole.ASSISTANT, aiMessage, modelName, tokenUsage, now));
            }
            return result;
        } catch (Throwable e) {
            log.warn("AI 调用失败, 已登记用户消息, sessionId={}", sessionId, e);
            chatMessageRepository.save(toEntity(sessionId, ChatRole.USER, userMessage, null, null, now));
            throw e;
        }
    }

    private String resolveModelName(Result<?> result) {
        String modelName = null;
        if (result.finalResponse() != null) {
            modelName = result.finalResponse().modelName();
        }
        return StringUtils.hasText(modelName) ? modelName : defaultModelName;
    }

    private ChatMessageEntity toEntity(String sessionId,
                                       ChatRole role,
                                       String content,
                                       String modelName,
                                       TokenUsage tokenUsage,
                                       LocalDateTime now) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setSessionId(sessionId);
        entity.setRole(role);
        entity.setContent(content);
        entity.setModelName(modelName);
        if (tokenUsage != null) {
            entity.setPromptTokens(tokenUsage.inputTokenCount());
            entity.setCompletionTokens(tokenUsage.outputTokenCount());
            entity.setTotalTokens(tokenUsage.totalTokenCount());
        }
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return entity;
    }
}
