package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/** Building - 楼栋实体，用于存储宿舍楼栋的基本信息 @author 王和友 @since 2026 */
@Data
@TableName("building")
public class Building {
    /** 楼栋ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 楼栋名称，如"1号楼"、"A栋"等 */
    private String buildingName;

    /** 楼栋编号，用于系统内部唯一标识楼栋 */
    private String buildingNumber;

    /** 楼层数量，该楼栋共有的楼层数 */
    private Integer floorCount;

    /** 房间数量，该楼栋共有的房间数 */
    private Integer roomCount;

    /** 管理员ID，关联管理员用户，指定该楼栋的管理负责人 */
    private Long managerId;

    /** 创建时间，记录该楼栋信息的创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，记录该楼栋信息的最后更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
