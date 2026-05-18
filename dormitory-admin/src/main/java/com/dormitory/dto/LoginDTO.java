package com.dormitory.dto;

import lombok.Data;

/**
 * LoginDTO - 登录请求数据传输对象
 * 
 * 用于接收用户登录请求的参数，包含用户名、密码和用户类型
 * 
 * @author 王和友
 * @since 2026
 */
@Data
public class LoginDTO {
    /** 用户名 */
    private String username;
    /** 密码 */
    private String password;
    /** 用户类型：1-学生，2-宿管，3-管理员 */
    private Integer userType;
}
