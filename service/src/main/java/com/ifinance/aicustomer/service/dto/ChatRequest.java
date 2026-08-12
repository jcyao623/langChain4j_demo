package com.ifinance.aicustomer.service.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 对话请求。
 *
 * @param message   用户消息
 * @param sessionId 会话 ID，为空时服务端自动创建
 */
public record ChatRequest(
        @NotBlank(message = "消息内容不能为空") String message,
        String sessionId) {
}
