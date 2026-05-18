package com.dormitory.service;

import com.dormitory.dto.LoginDTO;
import com.dormitory.dto.LoginVO;

/**
 * AuthService - 认证服务接口
 * 
 * 定义用户登录和登出的业务接口，支持学生、宿管、管理员三种用户类型的认证
 * 
 * @author 王和友
 * @since 2026
 */
public interface AuthService {
    /**
     * 用户登录
     * 
     * @param loginDTO 登录请求参数（用户名、密码、用户类型）
     * @param ip 登录IP地址
     * @return LoginVO 登录成功返回的用户信息包含Token
     */
    LoginVO login(LoginDTO loginDTO, String ip);
    
    /**
     * 用户登出
     * 
     * @param userId 用户ID
     * @param userType 用户类型
     */
    void logout(Long userId, Integer userType);
}
