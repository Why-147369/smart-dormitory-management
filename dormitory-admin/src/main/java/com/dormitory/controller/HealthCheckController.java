package com.dormitory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.common.Result;
import com.dormitory.entity.HealthCheck;
import com.dormitory.entity.Student;
import com.dormitory.entity.Room;
import com.dormitory.entity.Building;
import com.dormitory.entity.DormitoryManager;
import com.dormitory.entity.CivilizedDormitory;
import com.dormitory.mapper.HealthCheckMapper;
import com.dormitory.mapper.StudentMapper;
import com.dormitory.mapper.RoomMapper;
import com.dormitory.mapper.BuildingMapper;
import com.dormitory.mapper.DormitoryManagerMapper;
import com.dormitory.mapper.CivilizedDormitoryMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * HealthCheckController - 健康打卡控制器
 * 提供宿舍卫生检查、健康打卡等功能的RESTful接口
 * @author 王和友
 * @since 2026
 */
@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    private final HealthCheckMapper healthCheckMapper;
    private final StudentMapper studentMapper;
    private final RoomMapper roomMapper;
    private final BuildingMapper buildingMapper;
    private final DormitoryManagerMapper dormitoryManagerMapper;
    private final CivilizedDormitoryMapper civilizedDormitoryMapper;

    public HealthCheckController(HealthCheckMapper healthCheckMapper, StudentMapper studentMapper,
                              RoomMapper roomMapper, BuildingMapper buildingMapper, DormitoryManagerMapper dormitoryManagerMapper,
                              CivilizedDormitoryMapper civilizedDormitoryMapper) {
        this.healthCheckMapper = healthCheckMapper;
        this.studentMapper = studentMapper;
        this.roomMapper = roomMapper;
        this.buildingMapper = buildingMapper;
        this.dormitoryManagerMapper = dormitoryManagerMapper;
        this.civilizedDormitoryMapper = civilizedDormitoryMapper;
    }

    /**
     * 提交健康打卡/卫生检查记录
     * 自动设置打卡日期为当前日期
     * @param healthCheck 健康打卡对象
     * @return 打卡记录详情
     */
    @PostMapping("/check")
    public Result<HealthCheck> check(@RequestBody HealthCheck healthCheck) {
        healthCheck.setCheckDate(LocalDate.now());
        healthCheck.setCreateTime(LocalDateTime.now());
        healthCheckMapper.insert(healthCheck);
        return Result.success(healthCheck);
    }

    /**
     * 获取健康打卡记录列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param buildingId 楼栋ID（可选）
     * @param roomId 宿舍ID（可选）
     * @param managerId 管理员ID（可选）
     * @param studentId 学生ID（可选）
     * @return 分页后的打卡记录列表
     */
    @GetMapping("/list")
    public Result<Page<Map<String, Object>>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                                 @RequestParam(required = false) Long buildingId,
                                                 @RequestParam(required = false) Long roomId,
                                                 @RequestParam(required = false) Long managerId,
                                                 @RequestParam(required = false) Long studentId) {
        List<Room> allRooms = roomMapper.selectList(null);
        Map<Long, Room> roomMap = new HashMap<>();
        Set<Long> roomIds = new HashSet<>();
        for (Room r : allRooms) {
            roomMap.put(r.getId(), r);
            roomIds.add(r.getId());
        }

        Map<Long, Building> buildingMap = new HashMap<>();
        List<Building> buildings = buildingMapper.selectList(null);
        for (Building b : buildings) {
            buildingMap.put(b.getId(), b);
        }

        Map<Long, DormitoryManager> managerMap = new HashMap<>();
        List<DormitoryManager> managers = dormitoryManagerMapper.selectList(null);
        for (DormitoryManager m : managers) {
            managerMap.put(m.getId(), m);
        }

        Set<Long> filteredRoomIds = roomIds;
        if (buildingId != null) {
            filteredRoomIds = new HashSet<>();
            for (Room r : allRooms) {
                if (r.getBuildingId() != null && r.getBuildingId().equals(buildingId)) {
                    filteredRoomIds.add(r.getId());
                }
            }
        }

        if (studentId != null) {
            Student student = studentMapper.selectById(studentId);
            if (student != null && student.getRoomId() != null) {
                filteredRoomIds = new HashSet<>();
                filteredRoomIds.add(student.getRoomId());
            } else {
                filteredRoomIds = new HashSet<>();
            }
        }

        Page<HealthCheck> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HealthCheck> wrapper = new LambdaQueryWrapper<>();
        if (roomId != null) {
            wrapper.eq(HealthCheck::getRoomId, roomId);
        }
        if (managerId != null) {
            wrapper.eq(HealthCheck::getManagerId, managerId);
        }
        if (filteredRoomIds != null && !filteredRoomIds.isEmpty()) {
            wrapper.in(HealthCheck::getRoomId, filteredRoomIds);
        }
        wrapper.orderByDesc(HealthCheck::getCheckDate, HealthCheck::getCreateTime);
        Page<HealthCheck> result = healthCheckMapper.selectPage(page, wrapper);

        List<Map<String, Object>> records = new ArrayList<>();
        for (HealthCheck hc : result.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", hc.getId());
            map.put("roomId", hc.getRoomId());
            map.put("managerId", hc.getManagerId());
            map.put("score", hc.getScore());
            map.put("description", hc.getDescription());
            map.put("images", hc.getImages());
            map.put("checkDate", hc.getCheckDate());
            map.put("createTime", hc.getCreateTime());

            Room room = roomMap.get(hc.getRoomId());
            if (room != null) {
                map.put("roomNumber", room.getRoomNumber());
                Building building = buildingMap.get(room.getBuildingId());
                if (building != null) {
                    map.put("buildingName", building.getBuildingName());
                    map.put("buildingId", building.getId());
                }
            }

            DormitoryManager manager = managerMap.get(hc.getManagerId());
            if (manager != null) {
                map.put("managerName", manager.getName());
            }

            records.add(map);
        }

        Page<Map<String, Object>> resultPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        resultPage.setRecords(records);
        return Result.success(resultPage);
    }

    /**
     * 根据ID获取健康打卡记录详情
     * @param id 打卡记录ID
     * @return 打卡记录详情
     */
    @GetMapping("/{id}")
    public Result<HealthCheck> getById(@PathVariable Long id) {
        HealthCheck healthCheck = healthCheckMapper.selectById(id);
        return Result.success(healthCheck);
    }

    /**
     * 根据宿舍ID获取健康打卡记录
     * @param roomId 宿舍ID
     * @return 打卡记录列表
     */
    @GetMapping("/room/{roomId}")
    public Result<List<Map<String, Object>>> getByRoomId(@PathVariable Long roomId) {
        LambdaQueryWrapper<HealthCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthCheck::getRoomId, roomId);
        wrapper.orderByDesc(HealthCheck::getCheckDate);
        List<HealthCheck> list = healthCheckMapper.selectList(wrapper);

        Map<Long, DormitoryManager> managerMap = new HashMap<>();
        List<DormitoryManager> managers = dormitoryManagerMapper.selectList(null);
        for (DormitoryManager m : managers) {
            managerMap.put(m.getId(), m);
        }

        List<Map<String, Object>> records = new ArrayList<>();
        for (HealthCheck hc : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", hc.getId());
            map.put("score", hc.getScore());
            map.put("description", hc.getDescription());
            map.put("images", hc.getImages());
            map.put("checkDate", hc.getCheckDate());
            map.put("createTime", hc.getCreateTime());

            DormitoryManager manager = managerMap.get(hc.getManagerId());
            if (manager != null) {
                map.put("managerName", manager.getName());
            }
            records.add(map);
        }
        return Result.success(records);
    }

    /**
     * 获取指定宿舍的最新一条健康打卡记录
     * @param roomId 宿舍ID
     * @return 最新打卡记录
     */
    @GetMapping("/room/{roomId}/latest")
    public Result<HealthCheck> getLatestByRoomId(@PathVariable Long roomId) {
        LambdaQueryWrapper<HealthCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthCheck::getRoomId, roomId);
        wrapper.orderByDesc(HealthCheck::getCheckDate);
        wrapper.last("LIMIT 1");
        HealthCheck healthCheck = healthCheckMapper.selectOne(wrapper);
        return Result.success(healthCheck);
    }

    /**
     * 删除健康打卡记录
     * 同时删除关联的文明宿舍评选记录
     * @param id 打卡记录ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        HealthCheck healthCheck = healthCheckMapper.selectById(id);
        if (healthCheck != null && healthCheck.getCheckDate() != null) {
            LambdaQueryWrapper<CivilizedDormitory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CivilizedDormitory::getRoomId, healthCheck.getRoomId());
            wrapper.eq(CivilizedDormitory::getYear, healthCheck.getCheckDate().getYear());
            wrapper.eq(CivilizedDormitory::getMonth, healthCheck.getCheckDate().getMonthValue());
            civilizedDormitoryMapper.delete(wrapper);
        }
        healthCheckMapper.deleteById(id);
        return Result.success();
    }
    
    /**
     * 批量删除健康打卡记录
     * 同时删除关联的文明宿舍评选记录
     * @param ids 打卡记录ID列表
     * @return 操作结果
     */
    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            HealthCheck healthCheck = healthCheckMapper.selectById(id);
            if (healthCheck != null && healthCheck.getCheckDate() != null) {
                LambdaQueryWrapper<CivilizedDormitory> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(CivilizedDormitory::getRoomId, healthCheck.getRoomId());
                wrapper.eq(CivilizedDormitory::getYear, healthCheck.getCheckDate().getYear());
                wrapper.eq(CivilizedDormitory::getMonth, healthCheck.getCheckDate().getMonthValue());
                civilizedDormitoryMapper.delete(wrapper);
            }
        }
        healthCheckMapper.deleteBatchIds(ids);
        return Result.success();
    }
    
    /**
     * 批量提交健康打卡/卫生检查记录
     * 支持一次提交多个宿舍的检查记录
     * @param checks 健康打卡对象列表
     * @param managerId 管理员ID
     * @return 批量操作结果，包含成功数量、失败数量和错误信息
     */
    @PostMapping("/batch")
    public Result<Map<String, Object>> batchCheck(@RequestBody List<Map<String, Object>> checks, @RequestParam Long managerId) {
        Map<String, Object> result = new HashMap<>();
        int success = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();
        
        for (Map<String, Object> check : checks) {
            try {
                Object roomIdObj = check.get("roomId");
                Object scoreObj = check.get("score");
                Object descObj = check.get("description");
                
                if (roomIdObj == null || scoreObj == null) {
                    failed++;
                    errors.add("宿舍ID或得分不能为空");
                    continue;
                }
                
                Long roomId = null;
                if (roomIdObj instanceof Number) {
                    roomId = ((Number) roomIdObj).longValue();
                } else {
                    roomId = Long.parseLong(roomIdObj.toString());
                }
                
                Integer score = null;
                if (scoreObj instanceof Number) {
                    score = ((Number) scoreObj).intValue();
                } else {
                    score = Integer.parseInt(scoreObj.toString());
                }
                
                Room room = roomMapper.selectById(roomId);
                if (room == null) {
                    failed++;
                    errors.add("宿舍ID[" + roomId + "]不存在");
                    continue;
                }
                
                HealthCheck healthCheck = new HealthCheck();
                healthCheck.setRoomId(roomId);
                healthCheck.setManagerId(managerId);
                healthCheck.setScore(score);
                healthCheck.setDescription(descObj != null ? descObj.toString() : "");
                healthCheck.setCheckDate(LocalDate.now());
                healthCheck.setCreateTime(LocalDateTime.now());
                healthCheckMapper.insert(healthCheck);
                success++;
            } catch (Exception e) {
                failed++;
                errors.add("处理失败: " + e.getMessage());
            }
        }
        
        result.put("success", success);
        result.put("failed", failed);
        result.put("errors", errors);
        return Result.success(result);
    }
}
