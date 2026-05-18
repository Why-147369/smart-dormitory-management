package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Message - 消息实体，用于存储用户消息通知信息
 * @author 王和友
 * @since 2026
 */
@Data
@TableName("message")
public class Message {
    /** 消息ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收消息的用户ID */
    private Long userId;

    /** 用户类型：1-学生，2-宿管，3-管理员 */
    private Integer userType;

    /** 消息标题 */
    private String title;

    /** 消息内容 */
    private String content;

    /** 消息类型（如：系统消息、申请提醒等） */
    private String messageType;

    /** 是否已读：0-未读，1-已读 */
    private Integer isRead;

    /** 关联业务ID，可用于关联具体业务数据 */
    private Long relatedId;

    /** 消息创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
