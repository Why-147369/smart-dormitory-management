package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * RepairType - 报修类型实体，用于定义宿舍报修的分类类型
 * @author 王和友
 * @since 2026
 */
@Data
@TableName("repair_type")
public class RepairType {
    /** 报修类型唯一标识ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 类型名称，如：水电维修、门窗维修、家具维修等 */
    private String typeName;

    /** 类型图标，存储图标资源路径或图标名称 */
    private String typeIcon;

    /** 排序顺序，用于前端展示时的排序，数字越小越靠前 */
    private Integer sortOrder;

    /** 状态，0-禁用，1-启用 */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
