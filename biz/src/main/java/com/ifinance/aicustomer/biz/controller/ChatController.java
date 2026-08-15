package com.ifinance.aicustomer.biz.controller;

import com.ifinance.aicustomer.common.constant.CommonConstants;
import com.ifinance.aicustomer.common.model.Result;
import com.ifinance.aicustomer.service.dto.ChatMessageRecord;
import com.ifinance.aicustomer.service.dto.ChatRequest;
import com.ifinance.aicustomer.service.dto.ChatResponse;
import com.ifinance.aicustomer.service.service.ChatService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 智能客服接口。
 */
@RestController
@RequestMapping(CommonConstants.API_VERSION + "/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 发送消息并获取 AI 回复。
     */
    @PostMapping
    public Result<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("收到对话请求, sessionId={}", request.sessionId());
        return Result.ok(chatService.streamingChat(request));
    }

    /**
     * 查询会话历史记录。
     */
    @GetMapping("/history")
    public Result<List<ChatMessageRecord>> history(@RequestParam String sessionId) {
        log.info("查询历史记录, sessionId={}", sessionId);
        return Result.ok(chatService.history(sessionId));
    }
}
