package com.dormitory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.common.Constant;
import com.dormitory.common.Result;
import com.dormitory.entity.Building;
import com.dormitory.entity.DormitoryManager;
import com.dormitory.mapper.BuildingMapper;
import com.dormitory.mapper.DormitoryManagerMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ManagerController - 宿管管理控制器
 * 处理宿管人员的增删改查、密码重置、个人信息管理等请求
 * @author 王和友
 * @since 2026
 */
@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    private final DormitoryManagerMapper managerMapper;
    private final BuildingMapper buildingMapper;
    private final PasswordEncoder passwordEncoder;

    public ManagerController(DormitoryManagerMapper managerMapper,
                           BuildingMapper buildingMapper,
                           PasswordEncoder passwordEncoder) {
        this.managerMapper = managerMapper;
        this.buildingMapper = buildingMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 获取宿管列表
     * 分页查询宿管信息，支持按楼栋和关键字筛选
     * @param pageNum 页码，默认1
     * @param pageSize 每页数量，默认10
     * @param buildingId 楼栋ID（可选）
     * @param keyword 搜索关键字（可选）
     * @return 分页宿管列表
     */
    @GetMapping("/list")
    public Result<Page<Map<String, Object>>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              @RequestParam(required = false) Long buildingId,
                                              @RequestParam(required = false) String keyword) {
        Page<DormitoryManager> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DormitoryManager> wrapper = new LambdaQueryWrapper<>();
        if (buildingId != null) {
            wrapper.eq(DormitoryManager::getBuildingId, buildingId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(DormitoryManager::getName, keyword)
                    .or().like(DormitoryManager::getUsername, keyword));
        }
        Page<DormitoryManager> result = managerMapper.selectPage(page, wrapper);
        
        List<Long> buildingIds = result.getRecords().stream()
            .map(DormitoryManager::getBuildingId)
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());
        
        Map<Long, String> buildingMap = new HashMap<>();
        if (!buildingIds.isEmpty()) {
            List<Building> buildings = buildingMapper.selectBatchIds(buildingIds);
            for (Building b : buildings) {
                buildingMap.put(b.getId(), b.getBuildingName());
            }
        }
        
        List<Map<String, Object>> records = new java.util.ArrayList<>();
        for (DormitoryManager m : result.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("username", m.getUsername());
            map.put("name", m.getName());
            map.put("gender", m.getGender());
            map.put("phone", m.getPhone());
            map.put("buildingId", m.getBuildingId());
            map.put("status", m.getStatus());
            map.put("createTime", m.getCreateTime());
            if (m.getBuildingId() != null) {
                map.put("buildingName", buildingMap.get(m.getBuildingId()));
            }
            records.add(map);
        }
        
        Page<Map<String, Object>> resultPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        resultPage.setRecords(records);
        return Result.success(resultPage);
    }

    /**
     * 根据ID获取宿管信息
     * @param id 宿管ID
     * @return 宿管信息
     */
    @GetMapping("/{id}")
    public Result<DormitoryManager> getById(@PathVariable Long id) {
        DormitoryManager manager = managerMapper.selectById(id);
        if (manager != null) {
            manager.setPassword(null);
        }
        return Result.success(manager);
    }

    /**
     * 添加宿管
     * 创建新的宿管记录，默认密码为123456
     * @param manager 宿管信息
     * @return 添加结果
     */
    @PostMapping
    public Result<Void> add(@RequestBody DormitoryManager manager) {
        manager.setPassword(passwordEncoder.encode("123456"));
        manager.setStatus(Constant.STATUS_NORMAL);
        managerMapper.insert(manager);
        return Result.success();
    }

    /**
     * 更新宿管信息
     * 根据传入的宿管对象更新数据库记录
     * @param manager 宿管信息
     * @return 更新结果
     */
    @PutMapping
    public Result<Void> update(@RequestBody DormitoryManager manager) {
        managerMapper.updateById(manager);
        return Result.success();
    }

    /**
     * 重置宿管密码
     * 将指定宿管的密码重置为123456
     * @param id 宿管ID
     * @return 重置结果
     */
    @PutMapping("/password/{id}")
    public Result<Void> resetPassword(@PathVariable Long id) {
        DormitoryManager manager = managerMapper.selectById(id);
        manager.setPassword(passwordEncoder.encode("123456"));
        managerMapper.updateById(manager);
        return Result.success();
    }

    /**
     * 删除宿管
     * 根据ID删除宿管记录
     * @param id 宿管ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        managerMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 更新宿管状态
     * 启用或禁用宿管账号
     * @param id 宿管ID
     * @param params 包含状态值的Map
     * @return 更新结果
     */
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> params) {
        DormitoryManager manager = managerMapper.selectById(id);
        if (manager != null) {
            manager.setStatus(params.get("status"));
            managerMapper.updateById(manager);
        }
        return Result.success();
    }

    /**
     * 根据楼栋ID获取宿管信息
     * 查询指定楼栋的宿管人员
     * @param buildingId 楼栋ID
     * @return 宿管信息
     */
    @GetMapping("/building/{buildingId}")
    public Result<DormitoryManager> getByBuildingId(@PathVariable Long buildingId) {
        LambdaQueryWrapper<DormitoryManager> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DormitoryManager::getBuildingId, buildingId);
        DormitoryManager manager = managerMapper.selectOne(wrapper);
        if (manager != null) {
            manager.setPassword(null);
        }
        return Result.success(manager);
    }

    /**
     * 获取所有宿管列表
     * 返回所有宿管的基本信息
     * @return 宿管列表
     */
    @GetMapping("/all")
    public Result<List<DormitoryManager>> getAll() {
        List<DormitoryManager> managers = managerMapper.selectList(null);
        managers.forEach(m -> m.setPassword(null));
        return Result.success(managers);
    }

    /**
     * 获取当前登录宿管信息
     * 返回当前登录宿管的详细信息
     * @return 当前宿管信息
     */
    @GetMapping("/me")
    public Result<Map<String, Object>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        DormitoryManager manager = managerMapper.selectById(userId);
        if (manager == null) {
            return Result.error("宿管不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", manager.getId());
        result.put("username", manager.getUsername());
        result.put("name", manager.getName());
        result.put("phone", manager.getPhone());
        result.put("avatar", manager.getAvatar());
        result.put("buildingId", manager.getBuildingId());
        
        if (manager.getBuildingId() != null) {
            Building building = buildingMapper.selectById(manager.getBuildingId());
            if (building != null) {
                result.put("buildingName", building.getBuildingName());
            }
        }
        
        manager.setPassword(null);
        return Result.success(result);
    }

    /**
     * 更新当前宿管个人资料
     * 允许宿管修改自己的基本信息
     * @param params 包含姓名、电话、头像等信息的Map
     * @return 更新结果
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody Map<String, Object> params) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        DormitoryManager manager = managerMapper.selectById(userId);
        if (manager == null) {
            return Result.error("宿管不存在");
        }
        
        if (params.containsKey("name")) {
            manager.setName((String) params.get("name"));
        }
        if (params.containsKey("phone")) {
            manager.setPhone((String) params.get("phone"));
        }
        if (params.containsKey("avatar")) {
            manager.setAvatar((String) params.get("avatar"));
        }
        
        managerMapper.updateById(manager);
        return Result.success();
    }

    /**
     * 修改当前宿管密码
     * 验证原密码后修改为新密码
     * @param params 包含旧密码和新密码的Map
     * @return 修改结果
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> params) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        DormitoryManager manager = managerMapper.selectById(userId);
        if (manager == null) {
            return Result.error("宿管不存在");
        }
        
        if (!passwordEncoder.matches(oldPassword, manager.getPassword())) {
            return Result.error("原密码错误");
        }
        
        manager.setPassword(passwordEncoder.encode(newPassword));
        managerMapper.updateById(manager);
        return Result.success();
    }
}
