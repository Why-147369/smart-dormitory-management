package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * UtilityWarning - 水电费预警实体，用于记录宿舍水电用量超额的预警信息
 * @author 王和友 @since 2026
 */
@Data
@TableName("utility_warning")
public class UtilityWarning {
    
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 房间ID，关联宿舍房间信息 */
    private Long roomId;
    
    /** 楼宇ID，关联宿舍楼宇信息 */
    private Long buildingId;
    
    /** 预警年份 */
    private Integer year;
    
    /** 预警月份 */
    private Integer month;
    
    /** 用水量（吨） */
    private BigDecimal waterUsage;
    
    /** 用水限制（吨） */
    private BigDecimal waterLimit;
    
    /** 用电量（千瓦时） */
    private BigDecimal electricUsage;
    
    /** 用电限制（千瓦时） */
    private BigDecimal electricLimit;
    
    /** 是否用水超额：0-未超额，1-超额 */
    private Integer isWaterOver;
    
    /** 是否用电超额：0-未超额，1-超额 */
    private Integer isElectricOver;
    
    /** 预警状态：0-未处理，1-已处理 */
    private Integer status;
    
    /** 创建时间 */
    private LocalDateTime createTime;
}
