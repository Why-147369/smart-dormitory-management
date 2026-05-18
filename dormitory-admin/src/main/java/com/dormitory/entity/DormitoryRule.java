package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/** DormitoryRule - 宿舍规则实体，用于管理宿舍规章制度信息
 * @author 王和友
 * @since 2026
 */
@Data
@TableName("dormitory_rule")
public class DormitoryRule {
    /** 宿舍规则主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属宿舍楼ID，关联宿舍楼信息，0表示适用于所有宿舍楼 */
    private Long buildingId;

    /** 规则标题，宿舍规则的名称或标题 */
    private String title;

    /** 规则内容，宿舍规则的详细描述 */
    private String content;

    /** 规则类型：1-卫生检查标准, 2-安全管理规定, 3-作息管理规定, 4-公共设施使用规范 */
    private Integer ruleType;

    /** 记录创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 记录更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
