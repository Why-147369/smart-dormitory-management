package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/** MaintenancePerson - 维修人员实体，用于存储宿舍维修人员的基本信息 @author 王和友 @since 2026 */
@Data
@TableName("maintenance_person")
public class MaintenancePerson {
    /** 维修人员唯一标识ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 维修人员姓名 */
    private String name;

    /** 维修人员联系电话 */
    private String phone;

    /** 专业技能，描述维修人员擅长的维修领域 */
    private String specialty;

    /** 登录账号 */
    private String username;
    /** 登录密码(BCrypt加密) */
    private String password;
    /** 维修人员状态：0-空闲 1-工作中 2-休假 */
    private Integer status;
    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 创建时间，记录维修人员信息的创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，记录维修人员信息的最后更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
