package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/** Room - 宿舍实体，用于存储宿舍房间的基本信息 @author 王和友 @since 2026 */
@Data
@TableName("room")
public class Room {
    /** 宿舍ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属楼栋ID，关联楼栋表，标识该宿舍属于哪栋楼 */
    private Long buildingId;

    /** 房间号，如"101"、"202"等，用于标识具体宿舍房间 */
    private String roomNumber;

    /** 所在楼层，该宿舍所在的楼层数 */
    private Integer floor;

    /** 房间类型，如4人间、6人间等，标识宿舍的床位配置类型 */
    private Integer roomType;

    /** 床位总数，该宿舍可容纳的床位数量 */
    private Integer bedCount;

    /** 当前入住人数，当前已入住的学生数量 */
    private Integer currentCount;

    /** 宿舍状态，如可用、已满、检修等状态 */
    private Integer status;

    /** 创建时间，记录该宿舍信息的创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，记录该宿舍信息的最后更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
