package com.ifinance.aicustomer.service.service.impl;

import com.ifinance.aicustomer.common.exception.BusinessException;
import com.ifinance.aicustomer.common.exception.ErrorCode;
import com.ifinance.aicustomer.common.util.UuidUtils;
import com.ifinance.aicustomer.service.dto.ChatMessageRecord;
import com.ifinance.aicustomer.service.dto.ChatRequest;
import com.ifinance.aicustomer.service.dto.ChatResponse;
import com.ifinance.aicustomer.service.assistant.AiChatGateway;
import com.ifinance.aicustomer.service.entity.ChatMessageEntity;
import com.ifinance.aicustomer.service.mapper.ChatMessageMapper;
import com.ifinance.aicustomer.service.service.ChatService;
import dev.langchain4j.service.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final AiChatGateway aiChatGateway;
    private final ChatMessageMapper chatMessageMapper;
    private final String modelName;

    public ChatServiceImpl(AiChatGateway aiChatGateway,
                           ChatMessageMapper chatMessageMapper,
                           @Value("${openai-compatible.aliyun.model:qwen-plus}") String modelName) {
        this.aiChatGateway = aiChatGateway;
        this.chatMessageMapper = chatMessageMapper;
        this.modelName = modelName;
    }

    @Override
    /**
     * 处理一次对话请求，调用 AI Service 并返回结果。
     */
    public ChatResponse chat(ChatRequest request) {
        String sessionId = StringUtils.hasText(request.sessionId()) ? request.sessionId() : UuidUtils.generate();
        log.info("收到对话请求, sessionId={}, messageLength={}", sessionId, request.message().length());
        Result<String> result = aiChatGateway.chat(sessionId, request.message());
        log.info("AI 回复完成, sessionId={}, replyLength={}", sessionId, result.content().length());
        return new ChatResponse(sessionId, result.content(), LocalDateTime.now(), modelName);
    }

    @Override
    /**
     * 查询指定会话的历史聊天记录。
     */
    public List<ChatMessageRecord> history(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "sessionId 不能为空");
        }
        List<ChatMessageRecord> records = chatMessageMapper.findBySessionIdOrderByCreatedAtAsc(sessionId)
                .stream()
                .map(this::toRecord)
                .toList();
        log.debug("查询会话历史完成, sessionId={}, size={}", sessionId, records.size());
        return records;
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
