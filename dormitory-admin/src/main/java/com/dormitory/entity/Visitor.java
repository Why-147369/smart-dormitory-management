package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Visitor - 访客实体，用于记录访客来访登记和审批信息
 * @author 王和友
 * @since 2026
 */
@Data
@TableName("visitor")
public class Visitor {
    /** 访客记录ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 被访问的学生ID */
    private Long studentId;
    /** 访客姓名 */
    private String visitorName;
    /** 性别：0-女 1-男 */
    private Integer gender;
    /** 访客联系电话 */
    private String phone;
    /** 访客来源/单位 */
    private String source;
    /** 访问时间 */
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime visitTime;
    /** 访问目的 */
    private String purpose;
    /** 审批状态：0-待审批 1-已同意 2-已拒绝 */
    private Integer status;
    /** 审批人ID */
    private Long approverId;
    /** 拒绝原因 */
    private String rejectReason;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
