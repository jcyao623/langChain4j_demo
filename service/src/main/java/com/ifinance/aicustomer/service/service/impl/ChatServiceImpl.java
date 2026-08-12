package com.ifinance.aicustomer.service.service.impl;

import com.ifinance.aicustomer.common.constant.CommonConstants;
import com.ifinance.aicustomer.common.enums.ChatRole;
import com.ifinance.aicustomer.common.exception.BusinessException;
import com.ifinance.aicustomer.common.exception.ErrorCode;
import com.ifinance.aicustomer.common.util.UuidUtils;
import com.ifinance.aicustomer.service.dto.ChatMessageRecord;
import com.ifinance.aicustomer.service.dto.ChatRequest;
import com.ifinance.aicustomer.service.dto.ChatResponse;
import com.ifinance.aicustomer.service.entity.ChatMessageEntity;
import com.ifinance.aicustomer.service.repository.ChatMessageRepository;
import com.ifinance.aicustomer.service.service.ChatService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.output.TokenUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 智能客服对话服务实现。
 */
@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final ChatModel chatModel;
    private final ChatMessageRepository chatMessageRepository;
    private final String modelName;

    public ChatServiceImpl(ChatModel chatModel,
                           ChatMessageRepository chatMessageRepository,
                           @Value("${openai-compatible.aliyun.model:qwen-plus}") String modelName) {
        this.chatModel = chatModel;
        this.chatMessageRepository = chatMessageRepository;
        this.modelName = modelName;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        String sessionId = StringUtils.hasText(request.sessionId()) ? request.sessionId() : UuidUtils.generate();
        List<ChatMessage> messages = buildMessages(sessionId, request.message());

        LocalDateTime now = LocalDateTime.now();
        chatMessageRepository.save(toEntity(sessionId, ChatRole.USER, request.message(), null, null, now));

        try {
            dev.langchain4j.model.chat.response.ChatResponse response = chatModel.chat(messages);
            AiMessage aiMessage = response.aiMessage();
            TokenUsage tokenUsage = response.tokenUsage();
            String resolvedModelName = StringUtils.hasText(response.modelName()) ? response.modelName() : modelName;
            chatMessageRepository.save(
                    toEntity(sessionId, ChatRole.ASSISTANT, aiMessage.text(), resolvedModelName, tokenUsage, now));
            return new ChatResponse(sessionId, aiMessage.text(), now, resolvedModelName);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 服务调用失败, sessionId={}", sessionId, e);
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "AI 服务调用失败", e);
        }
    }

    @Override
    public List<ChatMessageRecord> history(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "sessionId 不能为空");
        }
        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId)
                .stream()
                .map(this::toRecord)
                .toList();
    }

    private List<ChatMessage> buildMessages(String sessionId, String userMessage) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.from(CommonConstants.DEFAULT_SYSTEM_PROMPT));
        List<ChatMessageEntity> history = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        for (ChatMessageEntity entity : history) {
            switch (entity.getRole()) {
                case USER -> messages.add(UserMessage.from(entity.getContent()));
                case ASSISTANT -> messages.add(AiMessage.from(entity.getContent()));
                case SYSTEM -> messages.add(SystemMessage.from(entity.getContent()));
                default -> log.warn("未知消息角色: {}", entity.getRole());
            }
        }
        messages.add(UserMessage.from(userMessage));
        return messages;
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

    private ChatMessageRecord toRecord(ChatMessageEntity entity) {
        return new ChatMessageRecord(
                entity.getId(),
                entity.getSessionId(),
                entity.getRole(),
                entity.getContent(),
                entity.getModelName(),
                entity.getCreatedAt());
    }
}
