package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Repair - 报修实体，用于存储学生宿舍报修信息
 * @author 王和友
 * @since 2026
 */
@Data
@TableName("repair")
public class Repair {
    /** 报修记录唯一标识ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 报修单号，系统生成的唯一报修编号 */
    private String repairNumber;

    /** 报修学生ID，关联学生信息表 */
    private Long studentId;

    /** 宿舍房间ID，关联房间信息表 */
    private Long roomId;

    /** 报修类型ID，关联报修类型表 */
    private Long typeId;

    /** 报修标题，简述报修内容 */
    private String title;

    /** 报修描述，详细说明报修情况 */
    private String description;

    /** 报修图片，存储图片路径，多个图片用逗号分隔 */
    private String images;

    /** 是否加急，0-普通报修，1-加急报修 */
    private Integer isEmergency;

    /** 报修状态，0-待处理，1-处理中，2-已完结，3-已取消 */
    private Integer status;

    /** 处理人ID，关联维修人员或宿管信息 */
    private Long handlerId;

    /** 维修人员姓名（旧字段，保留兼容） */
    private String repairPerson;
    /** 维修人员ID（关联maintenance_person.id） */
    private Long maintenanceId;
    /** 维修人员接单时间 */
    private LocalDateTime acceptTime;

    /** 处理备注，记录维修处理情况说明 */
    private String handleRemark;

    /** 创建时间，记录报修提交时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，记录最后修改时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
