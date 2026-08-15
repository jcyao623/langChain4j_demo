package com.ifinance.aicustomer.service.service.impl;

import com.ifinance.aicustomer.common.exception.BusinessException;
import com.ifinance.aicustomer.common.exception.ErrorCode;
import com.ifinance.aicustomer.common.util.StreamHelper;
import com.ifinance.aicustomer.common.util.UuidUtils;
import com.ifinance.aicustomer.service.assistant.AiStreamingChatGateway;
import com.ifinance.aicustomer.service.dto.ChatMessageRecord;
import com.ifinance.aicustomer.service.dto.ChatRequest;
import com.ifinance.aicustomer.service.dto.ChatResponse;
import com.ifinance.aicustomer.service.assistant.AiChatGateway;
import com.ifinance.aicustomer.service.entity.ChatMessageEntity;
import com.ifinance.aicustomer.service.mapper.ChatMessageMapper;
import com.ifinance.aicustomer.service.service.ChatService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.TokenStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 智能客服对话服务实现。
 */
@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final AiChatGateway aiChatGateway;
    private final AiStreamingChatGateway aiStreamingChatGateway;
    private final ChatMessageMapper chatMessageMapper;
    private final String modelName;

    public ChatServiceImpl(AiChatGateway aiChatGateway,
                           AiStreamingChatGateway aiStreamingChatGateway, ChatMessageMapper chatMessageMapper,
                           @Value("${openai-compatible.aliyun.model:qwen-plus}") String modelName) {
        this.aiChatGateway = aiChatGateway;
        this.aiStreamingChatGateway = aiStreamingChatGateway;
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
        Result<String> result = aiChatGateway.chat(sessionId, request.message(), LocalDate.now().toString());
        log.info("AI 回复完成, sessionId={}, replyLength={}", sessionId, result.content().length());
        return new ChatResponse(sessionId, result.content(), LocalDateTime.now(), modelName);
    }

    @Override
    public ChatResponse streamingChat(ChatRequest request) {
        String sessionId = StringUtils.hasText(request.sessionId()) ? request.sessionId() : UuidUtils.generate();
        log.info("收到对话请求,streamingChat sessionId={}, messageLength={}", sessionId, request.message().length());
        TokenStream  result = aiStreamingChatGateway.chat(sessionId, request.message(), LocalDate.now().toString());
        // 2. 使用 TokenStream 提供的收集方法，它会自动帮你阻塞并拼接完整字符串
        String fullText = StreamHelper.collectToString(result, 60); // 60 秒超时
        log.info("AI 回复完成fullText={}", fullText);
        log.info("AI 回复完成,streamingChat sessionId={}, replyLength={}", sessionId, fullText.length());
        return new ChatResponse(sessionId, fullText, LocalDateTime.now(), modelName);
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
