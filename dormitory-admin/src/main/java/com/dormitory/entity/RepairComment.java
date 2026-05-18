package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * RepairComment - 报修评价实体，用于存储学生对维修服务的评价信息
 * @author 王和友
 * @since 2026
 */
@Data
@TableName("repair_comment")
public class RepairComment {
    /** 评价记录唯一标识ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 报修记录ID，关联报修表，标识被评价的报修单 */
    private Long repairId;

    /** 评价学生ID，关联学生信息表 */
    private Long studentId;

    /** 评分，1-5分，表示学生对维修服务的满意程度 */
    private Integer rating;

    /** 评价内容，学生填写的评价文字说明 */
    private String content;

    /** 创建时间，记录评价提交时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
