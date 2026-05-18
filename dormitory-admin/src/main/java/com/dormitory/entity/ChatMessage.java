package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** ChatMessage - 聊天消息实体，用于存储聊天会话中的具体消息内容 @author 王和友 @since 2026 */
@Data
@TableName("chat_message")
public class ChatMessage {
    
    /** 消息唯一标识ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 所属会话ID，关联聊天会话表 */
    private Long sessionId;
    
    /** 发送者类型：1-学生 2-管理员 */
    private Integer senderType;
    
    /** 发送者ID，根据senderType关联学生表或管理员表 */
    private Long senderId;
    
    /** 消息内容 */
    private String messageContent;
    
    /** 消息发送时间 */
    private LocalDateTime createTime;
}
