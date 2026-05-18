package com.dormitory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.common.Constant;
import com.dormitory.common.Result;
import com.dormitory.entity.*;
import com.dormitory.mapper.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * MaintenancePersonController - 维修人员控制器，负责管理宿舍维修人员信息
 * @author 王和友
 * @since 2026
 */
@RestController
@RequestMapping("/api/maintenance")
public class MaintenancePersonController {

    private final MaintenancePersonMapper maintenancePersonMapper;
    private final RepairMapper repairMapper;
    private final StudentMapper studentMapper;
    private final RoomMapper roomMapper;
    private final BuildingMapper buildingMapper;
    private final RepairTypeMapper repairTypeMapper;
    private final MessageMapper messageMapper;
    private final PasswordEncoder passwordEncoder;

    public MaintenancePersonController(MaintenancePersonMapper maintenancePersonMapper,
                                       RepairMapper repairMapper, StudentMapper studentMapper,
                                       RoomMapper roomMapper, BuildingMapper buildingMapper,
                                       RepairTypeMapper repairTypeMapper, MessageMapper messageMapper,
                                       PasswordEncoder passwordEncoder) {
        this.maintenancePersonMapper = maintenancePersonMapper;
        this.repairMapper = repairMapper;
        this.studentMapper = studentMapper;
        this.roomMapper = roomMapper;
        this.buildingMapper = buildingMapper;
        this.repairTypeMapper = repairTypeMapper;
        this.messageMapper = messageMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 获取维修人员分页列表，支持按关键字搜索
     * @param pageNum 当前页码，默认1
     * @param pageSize 每页显示条数，默认10
     * @param keyword 搜索关键字（可选），支持按姓名、电话、专业搜索
     * @return 维修人员分页数据
     */
    @GetMapping("/list")
    public Result<Page<MaintenancePerson>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                               @RequestParam(required = false) String keyword) {
        Page<MaintenancePerson> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MaintenancePerson> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(MaintenancePerson::getName, keyword)
                    .or().like(MaintenancePerson::getPhone, keyword)
                    .or().like(MaintenancePerson::getSpecialty, keyword));
        }
        wrapper.orderByDesc(MaintenancePerson::getCreateTime);
        Page<MaintenancePerson> result = maintenancePersonMapper.selectPage(page, wrapper);
        return Result.success(result);
    }

    /**
     * 获取所有可用的维修人员列表
     * @return 所有状态正常的维修人员列表
     */
    @GetMapping("/all")
    public Result<List<MaintenancePerson>> getAll() {
        LambdaQueryWrapper<MaintenancePerson> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaintenancePerson::getStatus, Constant.STATUS_NORMAL);
        List<MaintenancePerson> list = maintenancePersonMapper.selectList(wrapper);
        return Result.success(list);
    }

    /**
     * 根据专业获取对应的维修人员列表
     * @param specialty 专业类型，如：水电、木工等
     * @return 指定专业的维修人员列表
     */
    @GetMapping("/by-type")
    public Result<List<MaintenancePerson>> getByType(@RequestParam String specialty) {
        LambdaQueryWrapper<MaintenancePerson> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaintenancePerson::getStatus, Constant.STATUS_NORMAL);
        wrapper.like(MaintenancePerson::getSpecialty, specialty);
        List<MaintenancePerson> list = maintenancePersonMapper.selectList(wrapper);
        return Result.success(list);
    }

    /**
     * 根据ID获取维修人员详细信息
     * @param id 维修人员ID
     * @return 维修人员详细信息
     */
    @GetMapping("/{id}")
    public Result<MaintenancePerson> getById(@PathVariable Long id) {
        MaintenancePerson person = maintenancePersonMapper.selectById(id);
        return Result.success(person);
    }

    /**
     * 添加新的维修人员
     * @param person 维修人员信息，包含姓名、电话、专业等
     * @return 操作结果
     */
    @PostMapping
    public Result<Void> add(@RequestBody MaintenancePerson person) {
        person.setPassword(passwordEncoder.encode(person.getPassword()));
        person.setStatus(Constant.STATUS_NORMAL);
        maintenancePersonMapper.insert(person);
        return Result.success();
    }

    /**
     * 更新维修人员信息
     * @param person 维修人员信息，包含ID及需要更新的字段
     * @return 操作结果
     */
    @PutMapping
    public Result<Void> update(@RequestBody MaintenancePerson person) {
        if (person.getPassword() != null && !person.getPassword().isEmpty()) {
            person.setPassword(passwordEncoder.encode(person.getPassword()));
        } else {
            MaintenancePerson existing = maintenancePersonMapper.selectById(person.getId());
            if (existing != null) person.setPassword(existing.getPassword());
        }
        maintenancePersonMapper.updateById(person);
        return Result.success();
    }

    /**
     * 删除维修人员
     * @param id 维修人员ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        maintenancePersonMapper.deleteById(id);
        return Result.success();
    }

    /** 维修人员修改自己的密码 */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestParam Long id,
                                        @RequestParam String oldPassword,
                                        @RequestParam String newPassword) {
        MaintenancePerson person = maintenancePersonMapper.selectById(id);
        if (person == null) return Result.error("用户不存在");
        if (!passwordEncoder.matches(oldPassword, person.getPassword())) return Result.error("原密码错误");
        person.setPassword(passwordEncoder.encode(newPassword));
        maintenancePersonMapper.updateById(person);
        return Result.success();
    }

    /**
     * 更新维修人员状态
     */
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        MaintenancePerson person = maintenancePersonMapper.selectById(id);
        person.setStatus(status);
        maintenancePersonMapper.updateById(person);
        return Result.success();
    }

    // ========== 维修人员自服务接口 ==========

    /** 获取当前登录维修人员信息 */
    @GetMapping("/me")
    public Result<MaintenancePerson> me(@RequestParam Long id) {
        return Result.success(maintenancePersonMapper.selectById(id));
    }

    /** 获取维修人员可见的工单列表：已分配给我的 + 未分配且类型匹配的 */
    @GetMapping("/repairs")
    public Result<Page<Map<String, Object>>> myRepairs(@RequestParam Long maintenanceId,
                                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                                        @RequestParam(required = false) Integer status) {
        MaintenancePerson person = maintenancePersonMapper.selectById(maintenanceId);
        if (person == null) return Result.error("维修人员不存在");

        // 找到类型匹配的未分配工单ID
        String specialty = person.getSpecialty() != null ? person.getSpecialty() : "";
        String[] keywords = specialty.split("[,，]");
        Set<Long> matchedIds = new HashSet<>();
        for (String kw : keywords) {
            String trimmed = kw.trim();
            if (trimmed.isEmpty()) continue;
            LambdaQueryWrapper<RepairType> rtWrapper = new LambdaQueryWrapper<>();
            rtWrapper.like(RepairType::getTypeName, trimmed);
            List<RepairType> types = repairTypeMapper.selectList(rtWrapper);
            for (RepairType t : types) {
                LambdaQueryWrapper<Repair> rWrapper = new LambdaQueryWrapper<>();
                rWrapper.eq(Repair::getTypeId, t.getId())
                        .eq(Repair::getStatus, Constant.REPAIR_STATUS_PENDING)
                        .and(w -> w.isNull(Repair::getMaintenanceId).or().eq(Repair::getMaintenanceId, 0L));
                List<Repair> repairs = repairMapper.selectList(rWrapper);
                for (Repair r : repairs) matchedIds.add(r.getId());
            }
        }

        // 查询：已分配给我的 or 类型匹配的未分配工单
        List<Repair> allRepairs = new ArrayList<>();
        // 已分配给我的
        LambdaQueryWrapper<Repair> myWrapper = new LambdaQueryWrapper<>();
        myWrapper.eq(Repair::getMaintenanceId, maintenanceId);
        if (status != null) myWrapper.eq(Repair::getStatus, status);
        allRepairs.addAll(repairMapper.selectList(myWrapper));
        // 类型匹配的未分配工单
        if (!matchedIds.isEmpty() && (status == null || status == Constant.REPAIR_STATUS_PENDING)) {
            for (Long id : matchedIds) {
                Repair r = repairMapper.selectById(id);
                if (r != null && (status == null || r.getStatus().equals(status))) {
                    if (allRepairs.stream().noneMatch(x -> x.getId().equals(r.getId()))) {
                        allRepairs.add(r);
                    }
                }
            }
        }

        // 排序
        allRepairs.sort((a, b) -> {
            int emergencyCompare = Integer.compare(b.getIsEmergency() != null ? b.getIsEmergency() : 0,
                                                   a.getIsEmergency() != null ? a.getIsEmergency() : 0);
            if (emergencyCompare != 0) return emergencyCompare;
            return b.getCreateTime().compareTo(a.getCreateTime());
        });

        // 手动分页
        int total = allRepairs.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<Repair> pageList = fromIndex < total ? allRepairs.subList(fromIndex, toIndex) : new ArrayList<>();

        List<Map<String, Object>> records = new ArrayList<>();
        for (Repair r : pageList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("repairNumber", r.getRepairNumber());
            map.put("title", r.getTitle());
            map.put("description", r.getDescription());
            map.put("status", r.getStatus());
            map.put("isEmergency", r.getIsEmergency());
            map.put("maintenanceId", r.getMaintenanceId());
            map.put("createTime", r.getCreateTime());
            Student stu = studentMapper.selectById(r.getStudentId());
            map.put("studentName", stu != null ? stu.getName() : "");
            map.put("studentNumber", stu != null ? stu.getStudentNumber() : "");
            if (stu != null && stu.getRoomId() != null) {
                Room room = roomMapper.selectById(stu.getRoomId());
                map.put("roomNumber", room != null ? room.getRoomNumber() : "");
            }
            if (r.getTypeId() != null) {
                RepairType rt = repairTypeMapper.selectById(r.getTypeId());
                map.put("typeName", rt != null ? rt.getTypeName() : "");
            }
            if (r.getMaintenanceId() != null && r.getMaintenanceId() > 0) {
                MaintenancePerson mp = maintenancePersonMapper.selectById(r.getMaintenanceId());
                map.put("maintenanceName", mp != null ? mp.getName() : "");
            }
            records.add(map);
        }
        Page<Map<String, Object>> resultPage = new Page<>(pageNum, pageSize, total);
        resultPage.setRecords(records);
        return Result.success(resultPage);
    }

    /** 维修人员接单 */
    @PutMapping("/repair/accept/{id}")
    public Result<Void> acceptRepair(@PathVariable Long id, @RequestParam Long maintenanceId) {
        Repair repair = repairMapper.selectById(id);
        if (repair.getMaintenanceId() != null && repair.getMaintenanceId() > 0
                && !repair.getMaintenanceId().equals(maintenanceId)) {
            return Result.error("该工单已被其他维修人员接单");
        }
        repair.setStatus(Constant.REPAIR_STATUS_REPAIRING);
        repair.setMaintenanceId(maintenanceId);
        repair.setAcceptTime(LocalDateTime.now());
        repair.setUpdateTime(LocalDateTime.now());
        repairMapper.updateById(repair);

        Student student = studentMapper.selectById(repair.getStudentId());
        if (student != null) {
            Message msg = new Message();
            msg.setUserId(student.getId());
            msg.setUserType(1);
            msg.setTitle("报修进度更新");
            msg.setContent("您的报修（" + repair.getRepairNumber() + "）维修人员已接单，正在维修中");
            msg.setIsRead(0);
            msg.setCreateTime(LocalDateTime.now());
            messageMapper.insert(msg);
        }
        return Result.success();
    }

    /** 维修人员完成维修 */
    @PutMapping("/repair/complete/{id}")
    public Result<Void> completeRepair(@PathVariable Long id,
                                        @RequestParam(required = false) String remark) {
        Repair repair = repairMapper.selectById(id);
        repair.setStatus(Constant.REPAIR_STATUS_COMPLETED);
        if (remark != null) repair.setHandleRemark(remark);
        repair.setUpdateTime(LocalDateTime.now());
        repairMapper.updateById(repair);

        Student student = studentMapper.selectById(repair.getStudentId());
        if (student != null) {
            Message msg = new Message();
            msg.setUserId(student.getId());
            msg.setUserType(1);
            msg.setTitle("报修已完成");
            msg.setContent("您的报修（" + repair.getRepairNumber() + "）已完成，请评价");
            msg.setIsRead(0);
            msg.setCreateTime(LocalDateTime.now());
            messageMapper.insert(msg);
        }
        return Result.success();
    }
}
