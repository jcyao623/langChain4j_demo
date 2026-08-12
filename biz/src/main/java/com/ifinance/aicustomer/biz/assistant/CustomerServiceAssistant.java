package com.ifinance.aicustomer.biz.assistant;

import com.ifinance.aicustomer.service.annotation.ChatRecord;
import com.ifinance.aicustomer.service.assistant.AiChatGateway;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

/**
 * 智能客服 AI Service，由 LangChain4j Spring Starter 自动生成代理。
 */
@AiService(wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "openAiChatModel", tools = "conversationContextTool",
        retrievalAugmentor = "retrievalAugmentor")
public interface CustomerServiceAssistant extends AiChatGateway {

    @SystemMessage("""
            你是一名互联网金融智能客服，回答需要专业、准确、简洁，并遵守金融合规要求。
            当前会话ID：{{sessionId}}。
            回答用户问题前，必须先调用 getChatHistory 工具查询当前会话的历史对话记录，
            并结合知识库检索到的内容作答。如果知识库没有相关内容，基于通用金融知识回答。
            """)
    @ChatRecord
    @Override
    /**
     * 执行一次智能客服对话。
     */
    Result<String> chat(@V("sessionId") String sessionId, @UserMessage String userMessage);
}
