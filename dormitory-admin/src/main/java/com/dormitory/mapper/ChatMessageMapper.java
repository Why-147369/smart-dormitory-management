package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * ChatMessageMapper - 聊天消息Mapper
 * 对应ChatMessage实体，用于处理聊天消息相关数据，包括消息发送、消息记录等功能
 * @author 王和友
 * @since 2026
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
