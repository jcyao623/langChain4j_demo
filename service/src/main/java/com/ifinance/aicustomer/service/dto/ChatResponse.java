package com.ifinance.aicustomer.service.dto;

import java.time.LocalDateTime;

/**
 * 对话响应。
 *
 * @param sessionId 会话 ID
 * @param message   AI 回复内容
 * @param createdAt 回复时间
 * @param modelName 模型名称
 */
public record ChatResponse(
        String sessionId,
        String message,
        LocalDateTime createdAt,
        String modelName) {
}
