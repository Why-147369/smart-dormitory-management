package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/** EmergencyHelp - 紧急求助实体，用于存储学生的紧急求助信息 @author 王和友 @since 2026 */
@Data
@TableName("emergency_help")
public class EmergencyHelp {
    /** 求助记录唯一标识ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发起求助的学生ID，关联用户表 */
    private Long studentId;

    /** 求助学生所在的宿舍房间ID，关联宿舍房间表 */
    private Long roomId;

    /** 求助内容，描述具体的问题或紧急情况 */
    private String content;

    /** 求助状态：0-待处理 1-处理中 2-已处理 3-已关闭 */
    private Integer status;

    /** 处理时间，记录问题被处理的时间 */
    private java.time.LocalDateTime handleTime;

    /** 处理备注，记录处理结果或处理说明 */
    private String handleRemark;

    /** 创建时间，记录求助记录的创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，记录求助记录的最后更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
