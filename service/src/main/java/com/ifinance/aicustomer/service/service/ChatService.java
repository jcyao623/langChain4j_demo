package com.ifinance.aicustomer.service.service;

import com.ifinance.aicustomer.service.dto.ChatMessageRecord;
import com.ifinance.aicustomer.service.dto.ChatRequest;
import com.ifinance.aicustomer.service.dto.ChatResponse;

import java.util.List;

/**
 * 智能客服对话服务。
 */
public interface ChatService {

    /**
     * 发送一条用户消息并获取 AI 回复，同时将双方消息写入数据库。
     */
    ChatResponse chat(ChatRequest request);

    /**
     * 查询指定会话的全部聊天记录。
     */
    List<ChatMessageRecord> history(String sessionId);
}
