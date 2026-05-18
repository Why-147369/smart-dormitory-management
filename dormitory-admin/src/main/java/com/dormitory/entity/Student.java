package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/** Student - 学生信息实体，用于存储学生的基本信息和住宿信息 @author 王和友 @since 2026 */
@Data
@TableName("student")
public class Student {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 学号，学生的唯一标识 */
    private String studentNumber;

    /** 登录密码 */
    private String password;

    /** 学生姓名 */
    private String name;

    /** 性别：0-女，1-男 */
    private Integer gender;

    /** 所属学院 */
    private String college;

    /** 所学专业 */
    private String major;

    /** 所在班级 */
    private String className;

    /** 联系电话 */
    private String phone;

    /** 头像图片URL */
    private String avatar;

    /** 所属楼栋ID */
    private Long buildingId;

    /** 所在宿舍房间ID */
    private Long roomId;

    /** 床位号：1-4 */
    private Integer bedNumber;

    /** 状态：0-禁用，1-正常 */
    private Integer status;

    /** 记录创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 记录最后更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
