package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** CheckOut - 退宿记录实体，用于管理学生的退宿申请和审批流程
 * @author 王和友
 * @since 2026
 */
@Data
@TableName("check_out")
public class CheckOut {
    /** 退宿记录主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 申请退宿的学生ID，关联学生信息 */
    private Long studentId;

    /** 学生所在宿舍房间ID，关联宿舍房间信息 */
    private Long roomId;

    /** 学生所睡床位ID，关联床位信息 */
    private Long bedId;

    /** 退宿原因，描述学生申请退宿的具体原因 */
    private String reason;

    /** 预计退宿日期，学生计划搬离宿舍的日期 */
    private LocalDate expectedDate;

    /** 退宿申请状态：0-待审核, 1-已通过, 2-已拒绝 */
    private Integer status;

    /** 审批人ID，审批退宿申请的管理员或辅导员ID */
    private Long approverId;

    /** 审批时间，管理员审批退宿申请的时间 */
    private java.time.LocalDateTime approveTime;

    /** 拒绝原因，审批拒绝时填写的拒绝理由 */
    private String rejectReason;

    /** 资产检查结果，退宿时对宿舍内公共资产的检查情况 */
    private String assetCheck;

    /** 记录创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 记录更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
