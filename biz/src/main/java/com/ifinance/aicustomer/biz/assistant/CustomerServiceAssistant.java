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
        retrievalAugmentor = "retrievalAugmentor",
        toolProvider = "mcpToolProvider")
public interface CustomerServiceAssistant extends AiChatGateway {

    @SystemMessage("""
            你是一名互联网金融智能客服，回答需要专业、准确、简洁，并遵守金融合规要求。
            当前会话ID：{{sessionId}}。
            回答用户问题前，必须先调用 getChatHistory 工具查询当前会话的历史对话记录，
            并结合知识库检索到的内容作答。历史记录仅用于理解上下文，必须只回答当前用户消息中的问题，
            禁止把历史中其他问题的回答当作当前答案。
            如果知识库没有相关内容且外部工具无法提供所需数据，明确告知用户暂时无法提供，
            不得编造数字、财务指标或机构来源。
            当用户询问股票行情、A 股个股数据、汇率、存款或贷款利率、基金净值等外部市场数据时，
            优先调用 MCP 提供的市场数据工具查询后再作答；工具不可用或未返回数据时，明确告知用户。
            回答引用数据时必须如实说明数据来源；工具返回标注为演示数据时，应说明为演示数据，
            不得声称来自中国人民银行等真实机构。
            """)
    @ChatRecord
    @Override
    /**
     * 执行一次智能客服对话。
     */
    Result<String> chat(@V("sessionId") String sessionId, @UserMessage String userMessage);
}
