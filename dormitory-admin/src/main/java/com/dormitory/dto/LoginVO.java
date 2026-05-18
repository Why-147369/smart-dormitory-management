package com.dormitory.dto;

import lombok.Data;

/**
 * LoginVO - 登录响应数据传输对象
 * 
 * 用于返回登录成功后的用户信息，包含Token和用户基本信息
 * 
 * @author 王和友
 * @since 2026
 */
@Data
public class LoginVO {
    /** JWT访问令牌 */
    private String token;
    /** 用户ID */
    private Long userId;
    /** 用户类型：1-学生，2-宿管，3-管理员 */
    private Integer userType;
    /** 用户名 */
    private String username;
    /** 真实姓名 */
    private String name;
    /** 所属楼栋ID（宿管用） */
    private Long buildingId;
}
