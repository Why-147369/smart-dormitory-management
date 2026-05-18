package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** ChatSession - 聊天会话实体，用于存储学生与管理员之间的聊天会话信息 @author 王和友 @since 2026 */
@Data
@TableName("chat_session")
public class ChatSession {
    
    /** 会话唯一标识ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 发起会话的学生ID，关联学生表 */
    private Long studentId;
    
    /** 会话类型：1-咨询会话 2-报修会话 3-投诉会话 4-建议会话 */
    private Integer chatType;
    
    /** 会话状态：0-进行中 1-已结束 */
    private Integer status;
    
    /** 管理员ID，关联管理员表，负责处理该会话 */
    private Long adminId;
    
    /** 会话创建时间 */
    private LocalDateTime createTime;
    
    /** 会话结束时间 */
    private LocalDateTime endTime;
}
