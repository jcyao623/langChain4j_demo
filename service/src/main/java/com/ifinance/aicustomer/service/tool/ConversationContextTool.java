package com.ifinance.aicustomer.service.tool;

import com.ifinance.aicustomer.service.entity.ChatMessageEntity;
import com.ifinance.aicustomer.service.repository.ChatMessageRepository;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 对话上下文查询工具，供模型在回答前获取历史对话。
 */
@Component
public class ConversationContextTool {

    private static final Logger log = LoggerFactory.getLogger(ConversationContextTool.class);

    private final ChatMessageRepository chatMessageRepository;

    public ConversationContextTool(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    @Tool("查询指定会话的历史对话记录，返回格式为“角色：内容”，按时间正序排列")
    /**
     * 查询指定会话的历史对话，供模型在回答前获取上下文。
     */
    public String getChatHistory(@P("会话ID") String sessionId) {
        List<ChatMessageEntity> history = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        log.debug("查询历史上下文, sessionId={}, size={}", sessionId, history.size());
        return history.stream()
                .map(message -> message.getRole() + "：" + message.getContent())
                .collect(Collectors.joining("\n"));
    }
}
