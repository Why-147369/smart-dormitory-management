package com.dormitory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dormitory.common.Result;
import com.dormitory.dto.LoginDTO;
import com.dormitory.dto.LoginVO;
import com.dormitory.entity.Bed;
import com.dormitory.entity.Building;
import com.dormitory.entity.Room;
import com.dormitory.entity.Student;
import com.dormitory.mapper.BedMapper;
import com.dormitory.mapper.BuildingMapper;
import com.dormitory.mapper.RoomMapper;
import com.dormitory.mapper.StudentMapper;
import com.dormitory.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AuthController - 登录认证控制器
 * 处理用户登录、登出和获取用户信息的请求
 * @author 王和友
 * @since 2026
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final StudentMapper studentMapper;
    private final BedMapper bedMapper;
    private final RoomMapper roomMapper;
    private final BuildingMapper buildingMapper;

    public AuthController(AuthService authService, StudentMapper studentMapper,
                          BedMapper bedMapper, RoomMapper roomMapper,
                          BuildingMapper buildingMapper) {
        this.authService = authService;
        this.studentMapper = studentMapper;
        this.bedMapper = bedMapper;
        this.roomMapper = roomMapper;
        this.buildingMapper = buildingMapper;
    }

    /**
     * 用户登录接口
     * 验证用户名密码，返回登录令牌和用户信息
     * @param loginDTO 登录请求参数，包含用户名和密码
     * @param request HTTP请求对象，用于获取客户端IP
     * @return 登录结果，包含登录令牌和用户信息
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        try {
            String ip = getClientIp(request);
            LoginVO loginVO = authService.login(loginDTO, ip);
            return Result.success(loginVO);
        } catch (Exception e) {
            return Result.error(401, e.getMessage());
        }
    }
    
    /**
     * 获取客户端IP地址
     * 优先从请求头中获取真实IP，支持代理环境
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 用户登出接口
     * 清除当前用户的登录状态和会话信息
     * @param request HTTP请求对象
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            Long userId = (Long) authentication.getPrincipal();
            Integer userType = (Integer) authentication.getDetails();
            authService.logout(userId, userType);
        }
        return Result.success();
    }

    /**
     * 获取当前登录用户信息
     * 根据用户类型返回对应的详细信息
     * @return 用户信息，包含用户ID、类型及个人详细信息
     */
    @GetMapping("/info")
    public Result<Object> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            Long userId = (Long) authentication.getPrincipal();
            Integer userType = (Integer) authentication.getDetails();
            
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("userType", userType);
            
            if (userType == 1) {
                Student student = studentMapper.selectById(userId);
                if (student != null) {
                    result.put("id", student.getId());
                    result.put("name", student.getName());
                    result.put("studentNumber", student.getStudentNumber());
                    result.put("college", student.getCollege());
                    result.put("major", student.getMajor());
                    result.put("className", student.getClassName());
                    result.put("phone", student.getPhone());
                    
                    if (student.getBuildingId() != null) {
                        Building building = buildingMapper.selectById(student.getBuildingId());
                        if (building != null) {
                            result.put("buildingId", building.getId());
                            result.put("buildingName", building.getBuildingName());
                        }
                    }
                    
                    if (student.getRoomId() != null) {
                        Room room = roomMapper.selectById(student.getRoomId());
                        if (room != null) {
                            result.put("roomId", room.getId());
                            result.put("roomNumber", room.getRoomNumber());
                        }
                    }
                    
                    if (student.getBedNumber() != null) {
                        result.put("bedNumber", student.getBedNumber());
                    }
                }
            } else if (userType == 3) {
                result.put("id", userId);
                result.put("name", "管理员");
            }
            
            return Result.success(result);
        }
        return Result.error(401, "未登录");
    }
}
