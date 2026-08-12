package com.ifinance.aicustomer.service.service.impl;

import com.ifinance.aicustomer.common.exception.BusinessException;
import com.ifinance.aicustomer.common.exception.ErrorCode;
import com.ifinance.aicustomer.common.util.UuidUtils;
import com.ifinance.aicustomer.service.dto.ChatMessageRecord;
import com.ifinance.aicustomer.service.dto.ChatRequest;
import com.ifinance.aicustomer.service.dto.ChatResponse;
import com.ifinance.aicustomer.service.assistant.AiChatGateway;
import com.ifinance.aicustomer.service.entity.ChatMessageEntity;
import com.ifinance.aicustomer.service.repository.ChatMessageRepository;
import com.ifinance.aicustomer.service.service.ChatService;
import dev.langchain4j.service.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 智能客服对话服务实现。
 */
@Service
public class ChatServiceImpl implements ChatService {

    private final AiChatGateway aiChatGateway;
    private final ChatMessageRepository chatMessageRepository;
    private final String modelName;

    public ChatServiceImpl(AiChatGateway aiChatGateway,
                           ChatMessageRepository chatMessageRepository,
                           @Value("${openai-compatible.aliyun.model:qwen-plus}") String modelName) {
        this.aiChatGateway = aiChatGateway;
        this.chatMessageRepository = chatMessageRepository;
        this.modelName = modelName;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        String sessionId = StringUtils.hasText(request.sessionId()) ? request.sessionId() : UuidUtils.generate();
        Result<String> result = aiChatGateway.chat(sessionId, request.message());
        return new ChatResponse(sessionId, result.content(), LocalDateTime.now(), modelName);
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
