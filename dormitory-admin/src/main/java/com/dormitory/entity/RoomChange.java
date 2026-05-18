package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * RoomChange - 换寝实体，用于记录学生的宿舍调换申请和审批信息
 * @author 王和友
 * @since 2026
 */
@Data
@TableName("room_change")
public class RoomChange {
    /** 换寝记录ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 申请换寝的学生ID */
    private Long studentId;
    /** 当前所在房间ID */
    private Long currentRoomId;
    /** 当前所在床位ID */
    private Long currentBedId;
    /** 目标房间ID */
    private Long targetRoomId;
    /** 目标床位ID */
    private Long targetBedId;
    /** 换寝类型：1-换房 2-换床位 */
    private Integer changeType;
    /** 换寝原因 */
    private String reason;
    /** 审批状态：0-待审批 1-已同意 2-已拒绝 */
    private Integer status;
    /** 审批人ID */
    private Long approverId;
    /** 审批时间 */
    private LocalDateTime approveTime;
    /** 拒绝原因 */
    private String rejectReason;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    public LocalDateTime getApproveTime() {
        return approveTime;
    }
    
    public void setApproveTime(LocalDateTime approveTime) {
        this.approveTime = approveTime;
    }
}
