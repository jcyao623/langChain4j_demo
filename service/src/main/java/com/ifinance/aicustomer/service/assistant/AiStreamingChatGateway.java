package com.ifinance.aicustomer.service.assistant;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.TokenStream;

/**
 * 智能客服 AI 调用网关，由接入层通过 {@code @AiService} 提供实现。
 */
public interface AiStreamingChatGateway {

    /**
     * 调用智能客服模型并返回结构化结果。
     */
    TokenStream chat(String sessionId, String userMessage, String currentDate);
}
