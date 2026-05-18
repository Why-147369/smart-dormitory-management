package com.dormitory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.common.Constant;
import com.dormitory.common.Result;
import com.dormitory.entity.Admin;
import com.dormitory.mapper.AdminMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AdminController - 管理员管理控制器
 * 处理管理员的增删改查、密码重置、个人信息管理等请求
 * @author 王和友
 * @since 2026
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminController(AdminMapper adminMapper, PasswordEncoder passwordEncoder) {
        this.adminMapper = adminMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 获取管理员列表
     * 分页查询管理员信息，支持关键字搜索
     * @param pageNum 页码，默认1
     * @param pageSize 每页数量，默认10
     * @param keyword 搜索关键字（可选）
     * @return 分页管理员列表
     */
    @GetMapping("/list")
    public Result<Page<Admin>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   @RequestParam(required = false) String keyword) {
        Page<Admin> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Admin::getName, keyword)
                    .or().like(Admin::getUsername, keyword));
        }
        Page<Admin> result = adminMapper.selectPage(page, wrapper);
        result.getRecords().forEach(a -> a.setPassword(null));
        return Result.success(result);
    }

    /**
     * 获取当前登录管理员信息
     * 返回当前登录管理员的详细信息
     * @return 当前管理员信息
     */
    @GetMapping("/me")
    public Result<Admin> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        Admin admin = adminMapper.selectById(userId);
        if (admin != null) {
            admin.setPassword(null);
        }
        return Result.success(admin);
    }

    /**
     * 根据ID获取管理员信息
     * @param id 管理员ID
     * @return 管理员信息
     */
    @GetMapping("/{id}")
    public Result<Admin> getById(@PathVariable Long id) {
        Admin admin = adminMapper.selectById(id);
        if (admin != null) {
            admin.setPassword(null);
        }
        return Result.success(admin);
    }

    /**
     * 添加管理员
     * 创建新的管理员记录，默认密码为123456
     * @param admin 管理员信息
     * @return 添加结果
     */
    @PostMapping
    public Result<Void> add(@RequestBody Admin admin) {
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setStatus(Constant.STATUS_NORMAL);
        adminMapper.insert(admin);
        return Result.success();
    }

    /**
     * 更新管理员信息
     * 根据传入的管理员对象更新数据库记录
     * @param admin 管理员信息
     * @return 更新结果
     */
    @PutMapping
    public Result<Void> update(@RequestBody Admin admin) {
        adminMapper.updateById(admin);
        return Result.success();
    }

    /**
     * 重置管理员密码
     * 将指定管理员的密码重置为123456
     * @param id 管理员ID
     * @return 重置结果
     */
    @PutMapping("/password/{id}")
    public Result<Void> resetPassword(@PathVariable Long id) {
        Admin admin = adminMapper.selectById(id);
        admin.setPassword(passwordEncoder.encode("123456"));
        adminMapper.updateById(admin);
        return Result.success();
    }

    /**
     * 删除管理员
     * 根据ID删除管理员记录
     * @param id 管理员ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 更新管理员状态
     * 启用或禁用管理员账号
     * @param id 管理员ID
     * @param status 状态值
     * @return 更新结果
     */
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Admin admin = adminMapper.selectById(id);
        admin.setStatus(status);
        adminMapper.updateById(admin);
        return Result.success();
    }

    /**
     * 获取所有管理员列表
     * 返回所有管理员的基本信息
     * @return 管理员列表
     */
    @GetMapping("/all")
    public Result<List<Admin>> getAll() {
        List<Admin> admins = adminMapper.selectList(null);
        admins.forEach(a -> a.setPassword(null));
        return Result.success(admins);
    }

    /**
     * 更新当前管理员个人资料
     * 允许管理员修改自己的基本信息
     * @param admin 包含姓名、电话、头像等信息的Admin对象
     * @return 更新结果
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody Admin admin) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        Admin currentAdmin = adminMapper.selectById(userId);
        if (admin.getName() != null) {
            currentAdmin.setName(admin.getName());
        }
        if (admin.getPhone() != null) {
            currentAdmin.setPhone(admin.getPhone());
        }
        if (admin.getAvatar() != null) {
            currentAdmin.setAvatar(admin.getAvatar());
        }
        adminMapper.updateById(currentAdmin);
        return Result.success();
    }

    /**
     * 修改当前管理员密码
     * 验证原密码后修改为新密码
     * @param passwordDTO 包含旧密码和新密码的对象
     * @return 修改结果
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody PasswordDTO passwordDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        Admin admin = adminMapper.selectById(userId);

        if (!passwordEncoder.matches(passwordDTO.getOldPassword(), admin.getPassword())) {
            return Result.error(400, "原密码错误");
        }

        admin.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        adminMapper.updateById(admin);
        return Result.success();
    }

    public static class PasswordDTO {
        private String oldPassword;
        private String newPassword;

        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}
