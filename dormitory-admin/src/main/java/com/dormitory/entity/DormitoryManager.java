package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/** DormitoryManager - 宿管实体，用于存储宿舍管理员的基本信息和负责楼栋 @author 王和友 @since 2026 */
@Data
@TableName("dormitory_manager")
public class DormitoryManager {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录用户名 */
    private String username;

    /** 登录密码 */
    private String password;

    /** 宿管姓名 */
    private String name;

    /** 性别：0-女，1-男 */
    private Integer gender;

    /** 联系电话 */
    private String phone;

    /** 头像图片URL */
    private String avatar;

    /** 负责的楼栋ID */
    private Long buildingId;

    /** 状态：0-禁用，1-正常 */
    private Integer status;

    /** 记录创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 记录最后更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
