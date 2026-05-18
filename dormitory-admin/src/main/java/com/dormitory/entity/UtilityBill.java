package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * UtilityBill - 水电费账单实体，用于存储宿舍水电费缴纳记录
 * @author 王和友 @since 2026
 */
@Data
@TableName("utility_bill")
public class UtilityBill {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 房间ID，关联宿舍房间信息 */
    private Long roomId;
    /** 账单年份 */
    private Integer year;
    /** 账单月份 */
    private Integer month;
    /** 用电量（千瓦时） */
    private java.math.BigDecimal electricUsage;
    /** 用水量（吨） */
    private java.math.BigDecimal waterUsage;
    /** 电费金额（元） */
    private java.math.BigDecimal electricFee;
    /** 水费金额（元） */
    private java.math.BigDecimal waterFee;
    /** 总费用（元） */
    private java.math.BigDecimal totalFee;
    /** 是否已支付：0-未支付，1-已支付 */
    private Integer isPaid;
    /** 支付时间 */
    private java.time.LocalDateTime payTime;
    /** 支付方式 */
    private String payMethod;
    /** 管理员ID，记录操作人员 */
    private Long managerId;
    
    /** 是否用水超额：0-未超额，1-超额 */
    private Integer isWaterOver;
    /** 是否用电超额：0-未超额，1-超额 */
    private Integer isElectricOver;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
