package com.dormitory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.common.Result;
import com.dormitory.entity.LoginLog;
import com.dormitory.entity.Student;
import com.dormitory.entity.DormitoryManager;
import com.dormitory.entity.Admin;
import com.dormitory.mapper.LoginLogMapper;
import com.dormitory.mapper.StudentMapper;
import com.dormitory.mapper.DormitoryManagerMapper;
import com.dormitory.mapper.AdminMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * LogController - 日志控制器，负责管理系统用户登录日志
 * @author 王和友
 * @since 2026
 */
@RestController
@RequestMapping("/api/log")
public class LogController {

    private final LoginLogMapper loginLogMapper;
    private final StudentMapper studentMapper;
    private final DormitoryManagerMapper dormitoryManagerMapper;
    private final AdminMapper adminMapper;

    public LogController(LoginLogMapper loginLogMapper,
                        StudentMapper studentMapper,
                        DormitoryManagerMapper dormitoryManagerMapper,
                        AdminMapper adminMapper) {
        this.loginLogMapper = loginLogMapper;
        this.studentMapper = studentMapper;
        this.dormitoryManagerMapper = dormitoryManagerMapper;
        this.adminMapper = adminMapper;
    }

    /**
     * 获取登录日志列表，支持按用户类型和日期筛选
     * @param pageNum 当前页码，默认1
     * @param pageSize 每页显示条数，默认10
     * @param userType 用户类型（可选）：1-学生，2-宿管员，3-管理员
     * @param startDate 开始日期（可选），格式：yyyy-MM-dd
     * @param userTypeFilter 用户类型过滤（可选），优先级高于userType
     * @return 登录日志分页数据，包含用户名称等信息
     */
    @GetMapping("/login/list")
    public Result<Page<Map<String, Object>>> loginList(@RequestParam(defaultValue = "1") Integer pageNum,
                                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                                       @RequestParam(required = false) Integer userType,
                                                       @RequestParam(required = false) String startDate,
                                                       @RequestParam(defaultValue = "3") Integer userTypeFilter) {
        Page<LoginLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        if (userType != null) {
            wrapper.eq(LoginLog::getUserType, userType);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(LoginLog::getLoginTime, LocalDateTime.parse(startDate + "T00:00:00"));
        }
        if (userTypeFilter != null) {
            wrapper.eq(LoginLog::getUserType, userTypeFilter);
        }
        wrapper.orderByDesc(LoginLog::getLoginTime);
        
        Page<LoginLog> result = loginLogMapper.selectPage(page, wrapper);
        
        List<Map<String, Object>> records = new ArrayList<>();
        for (LoginLog log : result.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", log.getId());
            map.put("userId", log.getUserId());
            map.put("userType", log.getUserType());
            map.put("ip", log.getIpAddress());
            map.put("loginTime", log.getLoginTime());
            map.put("status", log.getStatus());
            
            String userName = "";
            if (log.getUserType() == 1) {
                Student student = studentMapper.selectById(log.getUserId());
                if (student != null) {
                    userName = student.getName() + " (" + student.getStudentNumber() + ")";
                }
            } else if (log.getUserType() == 2) {
                DormitoryManager manager = dormitoryManagerMapper.selectById(log.getUserId());
                if (manager != null) {
                    userName = manager.getUsername();
                }
            } else if (log.getUserType() == 3) {
                Admin admin = adminMapper.selectById(log.getUserId());
                if (admin != null) {
                    userName = admin.getUsername();
                }
            }
            map.put("userName", userName);
            records.add(map);
        }
        
        Page<Map<String, Object>> resultPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        resultPage.setRecords(records);
        return Result.success(resultPage);
    }
}
