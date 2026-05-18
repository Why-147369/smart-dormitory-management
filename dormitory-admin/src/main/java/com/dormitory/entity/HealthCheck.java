package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * HealthCheck - 健康打卡实体，用于记录宿舍卫生检查评分信息
 * @author 王和友 @since 2026
 */
@Data
@TableName("health_check")
public class HealthCheck {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 房间ID，关联宿舍房间信息 */
    private Long roomId;
    /** 管理员ID，记录检查人员 */
    private Long managerId;
    /** 卫生评分（0-100分） */
    private Integer score;
    /** 检查描述，记录检查情况说明 */
    private String description;
    /** 检查图片，多张图片用逗号分隔 */
    private String images;
    /** 检查日期 */
    private LocalDate checkDate;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
