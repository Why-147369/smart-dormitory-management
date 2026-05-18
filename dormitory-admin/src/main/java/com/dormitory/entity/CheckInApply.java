package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * CheckInApply - 入住申请实体，用于管理学生宿舍入住申请信息
 * @author 王和友
 * @since 2026
 */
@Data
@TableName("check_in_apply")
public class CheckInApply {
    /** 入住申请ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 申请学生ID，关联学生信息 */
    private Long studentId;

    /** 申请日期 */
    private LocalDate applyDate;

    /** 申请原因 */
    private String reason;

    /** 审核状态：0-待审核，1-已通过，2-已拒绝 */
    private Integer status;

    /** 审批宿管ID，关联管理员信息 */
    private Long managerId;

    /** 拒绝原因，当申请被拒绝时填写 */
    private String rejectReason;

    /** 申请创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 申请更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
