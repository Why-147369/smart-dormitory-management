package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/** CivilizedDormitory - 文明宿舍评分实体，用于记录和评选文明宿舍的月度评分信息
 * @author 王和友
 * @since 2026
 */
@Data
@TableName("civilized_dormitory")
public class CivilizedDormitory {
    /** 文明宿舍评分记录主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 宿舍房间ID，关联宿舍房间信息 */
    private Long roomId;

    /** 评分年份，记录评分的年份 */
    private Integer year;

    /** 评分月份，记录评分的月份（1-12） */
    private Integer month;

    /** 总得分，该宿舍当月文明评分的总分 */
    private java.math.BigDecimal totalScore;

    /** 排名，当月文明宿舍评选的名次 */
    @TableField("`rank`")
    private Integer rank;

    /** 记录创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 记录更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
