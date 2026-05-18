package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * CheckIn - 入住记录实体，用于记录学生入住宿舍信息
 * @author 王和友
 * @since 2026
 */
@Data
@TableName("check_in")
public class CheckIn {
    /** 入住记录ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 入住学生ID，关联学生信息 */
    private Long studentId;

    /** 入住日期 */
    private java.time.LocalDate checkDate;

    /** 入住时间 */
    private java.time.LocalTime checkTime;

    /** 入住状态：0-待审核，1-已入住，2-已退宿 */
    private Integer status;

    /** 是否迟到：0-否，1-是 */
    private Integer isLate;

    /** 备注信息 */
    private String remark;

    /** 记录创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
