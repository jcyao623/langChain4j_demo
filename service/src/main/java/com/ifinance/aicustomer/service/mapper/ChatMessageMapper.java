package com.ifinance.aicustomer.service.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ifinance.aicustomer.service.entity.ChatMessageEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 聊天消息 Mapper。
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessageEntity> {

    /**
     * 按创建时间正序查询会话消息。
     */
    default List<ChatMessageEntity> findBySessionIdOrderByCreatedAtAsc(String sessionId) {
        return selectList(new LambdaQueryWrapper<ChatMessageEntity>()
                .eq(ChatMessageEntity::getSessionId, sessionId)
                .orderByAsc(ChatMessageEntity::getCreatedAt));
    }
}
