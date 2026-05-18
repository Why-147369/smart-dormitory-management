package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/** Bed - 床位实体，用于存储宿舍床位的基本信息 @author 王和友 @since 2026 */
@Data
@TableName("bed")
public class Bed {
    /** 床位ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属房间ID，关联宿舍表，标识该床位属于哪个宿舍 */
    private Long roomId;

    /** 床位号，如1号床、2号床等，用于标识宿舍内的具体床位 */
    private Integer bedNumber;

    /** 入住学生ID，关联学生表，记录当前占用该床位的学生，null表示空床位 */
    private Long studentId;

    /** 床位状态，如空闲、已占用、损坏等状态 */
    private Integer status;

    /** 创建时间，记录该床位信息的创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，记录该床位信息的最后更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
