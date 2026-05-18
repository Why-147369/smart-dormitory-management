package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * UtilityThreshold - 水电费阈值实体，用于配置不同房间类型的水电用量上限和单价
 * @author 王和友 @since 2026
 */
@Data
@TableName("utility_threshold")
public class UtilityThreshold {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 房间类型：1-四人间，2-六人间，3-八人间等 */
    private Integer roomType;
    /** 用电量限制（千瓦时/月） */
    private java.math.BigDecimal electricLimit;
    /** 用水量限制（吨/月） */
    private java.math.BigDecimal waterLimit;
    /** 电费单价（元/千瓦时） */
    private java.math.BigDecimal electricPrice;
    /** 水费单价（元/吨） */
    private java.math.BigDecimal waterPrice;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
