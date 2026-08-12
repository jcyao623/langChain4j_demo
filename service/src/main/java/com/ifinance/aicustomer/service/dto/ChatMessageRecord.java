package com.ifinance.aicustomer.service.dto;

import com.ifinance.aicustomer.common.enums.ChatRole;

import java.time.LocalDateTime;

/**
 * 聊天记录。
 *
 * @param id        消息 ID
 * @param sessionId 会话 ID
 * @param role      消息角色
 * @param content   消息内容
 * @param modelName 模型名称
 * @param createdAt 创建时间
 */
public record ChatMessageRecord(
        Long id,
        String sessionId,
        ChatRole role,
        String content,
        String modelName,
        LocalDateTime createdAt) {
}
