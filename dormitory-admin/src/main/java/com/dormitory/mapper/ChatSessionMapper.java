package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * ChatSessionMapper - 聊天会话Mapper
 * 对应ChatSession实体，用于处理聊天会话相关数据，包括会话创建、会话列表等功能
 * @author 王和友
 * @since 2026
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
