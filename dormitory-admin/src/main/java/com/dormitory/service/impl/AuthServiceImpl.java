package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dormitory.common.Constant;
import com.dormitory.dto.LoginDTO;
import com.dormitory.dto.LoginVO;
import com.dormitory.entity.*;
import com.dormitory.mapper.*;
import com.dormitory.service.AuthService;
import com.dormitory.utils.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * AuthServiceImpl - 认证服务实现类
 * 
 * 实现AuthService接口，处理用户登录验证逻辑，
 * 包括密码验证、账号状态检查、登录日志记录和JWT令牌生成
 * 
 * @author 王和友
 * @since 2026
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final StudentMapper studentMapper;
    private final DormitoryManagerMapper managerMapper;
    private final AdminMapper adminMapper;
    private final MaintenancePersonMapper maintenancePersonMapper;
    private final LoginLogMapper loginLogMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(StudentMapper studentMapper,
                          DormitoryManagerMapper managerMapper,
                          AdminMapper adminMapper,
                          MaintenancePersonMapper maintenancePersonMapper,
                          LoginLogMapper loginLogMapper,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder) {
        this.studentMapper = studentMapper;
        this.managerMapper = managerMapper;
        this.adminMapper = adminMapper;
        this.maintenancePersonMapper = maintenancePersonMapper;
        this.loginLogMapper = loginLogMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 用户登录处理
     * 
     * 根据用户类型（学生/宿管/管理员）查询用户信息，
     * 验证密码正确性，检查账号状态，记录登录日志，生成JWT令牌
     * 
     * @param loginDTO 登录请求参数
     * @param ip 客户端IP地址
     * @return LoginVO 登录结果包含Token和用户信息
     */
    @Override
    public LoginVO login(LoginDTO loginDTO, String ip) {
        String username = loginDTO.getUsername();
        Integer userType = loginDTO.getUserType();

        LoginVO loginVO = new LoginVO();
        LoginLog loginLog = new LoginLog();
        loginLog.setLoginTime(LocalDateTime.now());
        loginLog.setUsername(username);
        loginLog.setIpAddress(ip);

        if (userType == Constant.USER_TYPE_STUDENT) {
            LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Student::getStudentNumber, username);
            Student student = studentMapper.selectOne(wrapper);

            if (student == null) {
                loginLog.setStatus(0);
                loginLog.setUserType(userType);
                loginLogMapper.insert(loginLog);
                throw new RuntimeException("学号不存在");
            }

            if (!passwordEncoder.matches(loginDTO.getPassword(), student.getPassword())) {
                loginLog.setStatus(0);
                loginLog.setUserId(student.getId());
                loginLog.setUserType(userType);
                loginLogMapper.insert(loginLog);
                throw new RuntimeException("密码错误");
            }

            if (student.getStatus() == Constant.STATUS_DISABLED) {
                loginLog.setStatus(0);
                loginLog.setUserId(student.getId());
                loginLog.setUserType(userType);
                loginLogMapper.insert(loginLog);
                throw new RuntimeException("账号已被禁用");
            }

            loginVO.setUserId(student.getId());
            loginVO.setUserType(Constant.USER_TYPE_STUDENT);
            loginVO.setUsername(student.getStudentNumber());
            loginVO.setName(student.getName());
            loginVO.setBuildingId(student.getBuildingId());

            loginLog.setUserId(student.getId());
            loginLog.setUserType(userType);
            loginLog.setStatus(1);
            loginLogMapper.insert(loginLog);

        } else if (userType == Constant.USER_TYPE_MANAGER) {
            LambdaQueryWrapper<DormitoryManager> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DormitoryManager::getUsername, username);
            DormitoryManager manager = managerMapper.selectOne(wrapper);

            if (manager == null) {
                loginLog.setStatus(0);
                loginLog.setUserType(userType);
                loginLogMapper.insert(loginLog);
                throw new RuntimeException("用户名不存在");
            }

            if (!passwordEncoder.matches(loginDTO.getPassword(), manager.getPassword())) {
                loginLog.setStatus(0);
                loginLog.setUserId(manager.getId());
                loginLog.setUserType(userType);
                loginLogMapper.insert(loginLog);
                throw new RuntimeException("密码错误");
            }

            if (manager.getStatus() == Constant.STATUS_DISABLED) {
                loginLog.setStatus(0);
                loginLog.setUserId(manager.getId());
                loginLog.setUserType(userType);
                loginLogMapper.insert(loginLog);
                throw new RuntimeException("账号已被禁用");
            }

            loginVO.setUserId(manager.getId());
            loginVO.setUserType(Constant.USER_TYPE_MANAGER);
            loginVO.setUsername(manager.getUsername());
            loginVO.setName(manager.getName());
            loginVO.setBuildingId(manager.getBuildingId());

            loginLog.setUserId(manager.getId());
            loginLog.setUserType(userType);
            loginLog.setStatus(1);
            loginLogMapper.insert(loginLog);

        } else if (userType == Constant.USER_TYPE_ADMIN) {
            LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Admin::getUsername, username);
            Admin admin = adminMapper.selectOne(wrapper);

            if (admin == null) {
                loginLog.setStatus(0);
                loginLog.setUserType(userType);
                loginLogMapper.insert(loginLog);
                throw new RuntimeException("用户名不存在");
            }

            if (!passwordEncoder.matches(loginDTO.getPassword(), admin.getPassword())) {
                loginLog.setStatus(0);
                loginLog.setUserId(admin.getId());
                loginLog.setUserType(userType);
                loginLogMapper.insert(loginLog);
                throw new RuntimeException("密码错误");
            }

            if (admin.getStatus() == Constant.STATUS_DISABLED) {
                loginLog.setStatus(0);
                loginLog.setUserId(admin.getId());
                loginLog.setUserType(userType);
                loginLogMapper.insert(loginLog);
                throw new RuntimeException("账号已被禁用");
            }

            loginVO.setUserId(admin.getId());
            loginVO.setUserType(Constant.USER_TYPE_ADMIN);
            loginVO.setUsername(admin.getUsername());
            loginVO.setName(admin.getName());

            loginLog.setUserId(admin.getId());
            loginLog.setUserType(userType);
            loginLog.setStatus(1);
            loginLogMapper.insert(loginLog);

        } else if (userType == Constant.USER_TYPE_MAINTENANCE) {
            LambdaQueryWrapper<MaintenancePerson> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MaintenancePerson::getUsername, username);
            MaintenancePerson person = maintenancePersonMapper.selectOne(wrapper);

            if (person == null) {
                loginLog.setStatus(0);
                loginLog.setUserType(userType);
                loginLogMapper.insert(loginLog);
                throw new RuntimeException("账号不存在");
            }

            if (!passwordEncoder.matches(loginDTO.getPassword(), person.getPassword())) {
                loginLog.setStatus(0);
                loginLog.setUserId(person.getId());
                loginLog.setUserType(userType);
                loginLogMapper.insert(loginLog);
                throw new RuntimeException("密码错误");
            }

            if (person.getStatus() == Constant.STATUS_DISABLED) {
                loginLog.setStatus(0);
                loginLog.setUserId(person.getId());
                loginLog.setUserType(userType);
                loginLogMapper.insert(loginLog);
                throw new RuntimeException("账号已被禁用");
            }

            person.setLastLoginTime(LocalDateTime.now());
            maintenancePersonMapper.updateById(person);

            loginVO.setUserId(person.getId());
            loginVO.setUserType(Constant.USER_TYPE_MAINTENANCE);
            loginVO.setUsername(person.getUsername());
            loginVO.setName(person.getName());

            loginLog.setUserId(person.getId());
            loginLog.setUserType(userType);
            loginLog.setStatus(1);
            loginLogMapper.insert(loginLog);
        }

        String token = jwtUtil.generateToken(loginVO.getUserId(), loginVO.getUserType(), loginVO.getUsername());
        loginVO.setToken(token);

        return loginVO;
    }

    /**
     * 用户登出处理（暂未实现）
     * 
     * @param userId 用户ID
     * @param userType 用户类型
     */
    @Override
    public void logout(Long userId, Integer userType) {
    }
}
