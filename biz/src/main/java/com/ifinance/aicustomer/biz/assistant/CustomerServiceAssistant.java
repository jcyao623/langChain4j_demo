package com.ifinance.aicustomer.biz.assistant;

import com.ifinance.aicustomer.service.annotation.ChatRecord;
import com.ifinance.aicustomer.service.assistant.AiChatGateway;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

/**
 * 智能客服 AI Service，由 LangChain4j Spring Starter 自动生成代理。
 */
@AiService(chatModel = "openAiChatModel", tools = "conversationContextTool")
public interface CustomerServiceAssistant extends AiChatGateway {

    @SystemMessage("""
            你是一名互联网金融智能客服，回答需要专业、准确、简洁，并遵守金融合规要求。
            当前会话ID：{{sessionId}}。
            回答用户问题前，必须先调用 getChatHistory 工具查询当前会话的历史对话记录，
            结合历史上下文后再回答。
            """)
    @ChatRecord
    @Override
    Result<String> chat(@V("sessionId") String sessionId, @UserMessage String userMessage);
}
