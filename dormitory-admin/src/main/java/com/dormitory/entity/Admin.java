package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/** Admin - 管理员实体，用于存储系统管理员的基本信息 @author 王和友 @since 2026 */
@Data
@TableName("admin")
public class Admin {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录用户名 */
    private String username;

    /** 登录密码 */
    private String password;

    /** 管理员姓名 */
    private String name;

    /** 联系电话 */
    private String phone;

    /** 头像图片URL */
    private String avatar;

    /** 状态：0-禁用，1-正常 */
    private Integer status;

    /** 记录创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 记录最后更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
