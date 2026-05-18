package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Notice - 通知公告实体，用于存储宿舍通知信息
 * @author 王和友
 * @since 2026
 */
@Data
@TableName("notice")
public class Notice {
    /** 通知ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 通知类型（如：系统通知、活动通知等） */
    private String noticeType;

    /** 是否置顶：0-否，1-是 */
    @TableField("is_top")
    private Integer isTop;

    /** 发布人ID，关联用户表 */
    private Long publisherId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
