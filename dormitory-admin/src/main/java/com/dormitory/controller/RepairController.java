package com.dormitory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.common.Constant;
import com.dormitory.common.Result;
import com.dormitory.entity.Repair;
import com.dormitory.entity.RepairType;
import com.dormitory.entity.Student;
import com.dormitory.entity.Bed;
import com.dormitory.entity.Room;
import com.dormitory.entity.Building;
import com.dormitory.entity.Message;
import com.dormitory.mapper.RepairMapper;
import com.dormitory.mapper.RepairTypeMapper;
import com.dormitory.mapper.StudentMapper;
import com.dormitory.mapper.BedMapper;
import com.dormitory.mapper.RoomMapper;
import com.dormitory.mapper.BuildingMapper;
import com.dormitory.mapper.MessageMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** RepairController - 报修管理控制器，提供报修的提交、查询、处理、类型管理等功能
 * @author 王和友 @since 2026 */
@RestController
@RequestMapping("/api/repair")
public class RepairController {

    private final RepairMapper repairMapper;
    private final RepairTypeMapper repairTypeMapper;
    private final StudentMapper studentMapper;
    private final BedMapper bedMapper;
    private final RoomMapper roomMapper;
    private final BuildingMapper buildingMapper;
    private final MessageMapper messageMapper;

    public RepairController(RepairMapper repairMapper, RepairTypeMapper repairTypeMapper,
                           StudentMapper studentMapper, BedMapper bedMapper,
                           RoomMapper roomMapper, BuildingMapper buildingMapper,
                           MessageMapper messageMapper) {
        this.repairMapper = repairMapper;
        this.repairTypeMapper = repairTypeMapper;
        this.studentMapper = studentMapper;
        this.bedMapper = bedMapper;
        this.roomMapper = roomMapper;
        this.buildingMapper = buildingMapper;
        this.messageMapper = messageMapper;
    }

    /** 获取报修类型列表
     * @return 报修类型列表 */
    @Cacheable(value = "repairTypeList")
    @GetMapping("/type/list")
    public Result<List<RepairType>> typeList() {
        LambdaQueryWrapper<RepairType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepairType::getStatus, Constant.STATUS_NORMAL);
        wrapper.orderByAsc(RepairType::getSortOrder);
        List<RepairType> types = repairTypeMapper.selectList(wrapper);
        return Result.success(types);
    }

    /** 提交报修申请
     * @param repair 报修信息
     * @return 提交成功后的报修信息 */
    @PostMapping("/submit")
    public Result<Repair> submit(@RequestBody Repair repair) {
        if (repair.getTitle() == null || repair.getTitle().trim().isEmpty()) return Result.error("报修标题不能为空");
        if (repair.getTypeId() == null) return Result.error("请选择报修类型");
        if (repair.getStudentId() != null) {
            Student student = studentMapper.selectById(repair.getStudentId());
            if (student != null && student.getRoomId() != null) {
                repair.setRoomId(student.getRoomId());
            } else if (student != null) {
                LambdaQueryWrapper<Bed> bedWrapper = new LambdaQueryWrapper<>();
                bedWrapper.eq(Bed::getStudentId, student.getId());
                Bed bed = bedMapper.selectOne(bedWrapper);
                if (bed != null) {
                    repair.setRoomId(bed.getRoomId());
                }
            }
        }
        String repairNumber = "BX" + System.currentTimeMillis();
        repair.setRepairNumber(repairNumber);
        repair.setStatus(Constant.REPAIR_STATUS_PENDING);
        repair.setCreateTime(LocalDateTime.now());
        repairMapper.insert(repair);
        return Result.success(repair);
    }

    /** 获取报修列表，支持多条件筛选和分页
     * @param pageNum 页码，默认1
     * @param pageSize 每页条数，默认10
     * @param studentId 学生ID（可选）
     * @param status 报修状态（可选）
     * @param typeId 报修类型ID（可选）
     * @param buildingId 楼栋ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 分页后的报修列表 */
    @GetMapping("/list")
    public Result<Page<Map<String, Object>>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                    @RequestParam(required = false) Long studentId,
                                    @RequestParam(required = false) Integer status,
                                    @RequestParam(required = false) Long typeId,
                                    @RequestParam(required = false) Long buildingId,
                                    @RequestParam(required = false) String startDate,
                                    @RequestParam(required = false) String endDate) {
        Page<Repair> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Repair> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) {
            wrapper.eq(Repair::getStudentId, studentId);
        }
        if (status != null) {
            wrapper.eq(Repair::getStatus, status);
        }
        if (typeId != null) {
            wrapper.eq(Repair::getTypeId, typeId);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(Repair::getCreateTime, startDate + " 00:00:00");
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(Repair::getCreateTime, endDate + " 23:59:59");
        }
        wrapper.orderByDesc(Repair::getCreateTime);
        Page<Repair> result = repairMapper.selectPage(page, wrapper);
        
        List<RepairType> allTypes = repairTypeMapper.selectList(null);
        Map<Long, String> typeMap = new HashMap<>();
        for (RepairType type : allTypes) {
            typeMap.put(type.getId(), type.getTypeName());
        }

        List<Long> studentIds = result.getRecords().stream().map(Repair::getStudentId).distinct().collect(java.util.stream.Collectors.toList());
        Map<Long, String> studentNameMap = new HashMap<>();
        Map<Long, String> studentRoomMap = new HashMap<>();
        Map<Long, Long> studentBuildingIdMap = new HashMap<>();
        if (!studentIds.isEmpty()) {
            List<Student> students = studentMapper.selectBatchIds(studentIds);
            for (Student s : students) {
                studentNameMap.put(s.getId(), s.getName());
                if (s.getRoomId() != null) {
                    studentRoomMap.put(s.getId(), s.getRoomId().toString());
                    studentBuildingIdMap.put(s.getId(), s.getBuildingId());
                }
            }
            List<Bed> beds = bedMapper.selectList(new LambdaQueryWrapper<Bed>().in(Bed::getStudentId, studentIds));
            for (Bed bed : beds) {
                if (bed.getStudentId() != null) {
                    studentRoomMap.put(bed.getStudentId(), bed.getRoomId().toString());
                    if (bed.getRoomId() != null) {
                        Room room = roomMapper.selectById(bed.getRoomId());
                        if (room != null) {
                            studentBuildingIdMap.put(bed.getStudentId(), room.getBuildingId());
                        }
                    }
                }
            }
        }

        List<Long> buildingIds = studentBuildingIdMap.values().stream()
            .filter(id -> id != null)
            .distinct()
            .collect(java.util.stream.Collectors.toList());
        Map<Long, String> buildingNameMap = new HashMap<>();
        if (!buildingIds.isEmpty()) {
            List<Building> buildings = buildingMapper.selectBatchIds(buildingIds);
            for (Building b : buildings) {
                buildingNameMap.put(b.getId(), b.getBuildingName());
            }
        }

        List<Map<String, Object>> records = new ArrayList<>();
        for (Repair repair : result.getRecords()) {
            Long repairBuildingId = studentBuildingIdMap.get(repair.getStudentId());
            if (buildingId != null && !buildingId.equals(repairBuildingId)) {
                continue;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", repair.getId());
            map.put("repairNumber", repair.getRepairNumber());
            map.put("studentId", repair.getStudentId());
            map.put("studentName", studentNameMap.get(repair.getStudentId()));
            map.put("roomId", repair.getRoomId());
            map.put("roomNumber", studentRoomMap.get(repair.getStudentId()));
            map.put("buildingId", studentBuildingIdMap.get(repair.getStudentId()));
            map.put("buildingName", buildingNameMap.get(studentBuildingIdMap.get(repair.getStudentId())));
            map.put("typeId", repair.getTypeId());
            map.put("repairTypeName", typeMap.get(repair.getTypeId()));
            map.put("title", repair.getTitle());
            map.put("description", repair.getDescription());
            map.put("images", repair.getImages());
            map.put("isEmergency", repair.getIsEmergency());
            map.put("status", repair.getStatus());
            map.put("handlerId", repair.getHandlerId());
            map.put("repairPerson", repair.getRepairPerson());
            map.put("handleRemark", repair.getHandleRemark());
            map.put("createTime", repair.getCreateTime());
            map.put("updateTime", repair.getUpdateTime());
            records.add(map);
        }
        
        Page<Map<String, Object>> resultPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        resultPage.setRecords(records);
        return Result.success(resultPage);
    }

    /** 根据ID获取报修详情
     * @param id 报修ID
     * @return 报修详细信息 */
    @GetMapping("/{id}")
    public Result<Repair> getById(@PathVariable Long id) {
        Repair repair = repairMapper.selectById(id);
        return Result.success(repair);
    }

    /** 根据ID获取报修类型详情
     * @param id 报修类型ID
     * @return 报修类型详细信息 */
    @GetMapping("/type/{id}")
    public Result<RepairType> getTypeById(@PathVariable Long id) {
        RepairType type = repairTypeMapper.selectById(id);
        return Result.success(type);
    }

    /** 取消报修（只能取消待处理的报修单）
     * @param id 报修ID
     * @return 操作结果 */
    @PutMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        Repair repair = repairMapper.selectById(id);
        if (repair.getStatus() != Constant.REPAIR_STATUS_PENDING) {
            return Result.error("只有待处理的报修单可以取消");
        }
        repair.setStatus(Constant.REPAIR_STATUS_CANCELLED);
        repairMapper.updateById(repair);
        return Result.success();
    }

    /** 受理报修
     * @param id 报修ID
     * @return 操作结果 */
    @PutMapping("/accept/{id}")
    public Result<Void> accept(@PathVariable Long id) {
        Repair repair = repairMapper.selectById(id);
        repair.setStatus(Constant.REPAIR_STATUS_ACCEPTED);
        repairMapper.updateById(repair);
        return Result.success();
    }

    /** 开始处理报修
     * @param id 报修ID
     * @return 操作结果 */
    @PutMapping("/start/{id}")
    public Result<Void> startRepair(@PathVariable Long id) {
        Repair repair = repairMapper.selectById(id);
        repair.setStatus(Constant.REPAIR_STATUS_REPAIRING);
        repairMapper.updateById(repair);
        return Result.success();
    }

    /** 完成报修处理
     * @param id 报修ID
     * @param remark 处理备注（可选）
     * @param repairPerson 维修人员（可选）
     * @return 操作结果，完成后通知学生 */
    @PutMapping("/complete/{id}")
    public Result<Void> complete(@PathVariable Long id,
                                 @RequestParam(required = false) String remark,
                                 @RequestParam(required = false) String repairPerson) {
        Repair repair = repairMapper.selectById(id);
        repair.setStatus(Constant.REPAIR_STATUS_COMPLETED);
        if (remark != null) {
            repair.setHandleRemark(remark);
        }
        if (repairPerson != null) {
            repair.setRepairPerson(repairPerson);
        }
        repair.setUpdateTime(LocalDateTime.now());
        repairMapper.updateById(repair);

        // 发送消息通知学生
        Student student = studentMapper.selectById(repair.getStudentId());
        if (student != null) {
            Message message = new Message();
            message.setUserId(student.getId());
            message.setUserType(1); // 1=学生
            message.setTitle("报修申请");
            message.setContent("报修已完成");
            message.setMessageType("报修通知");
            message.setIsRead(0);
            message.setCreateTime(LocalDateTime.now());
            messageMapper.insert(message);
        }
        return Result.success();
    }

    /** 获取学生的报修记录
     * @param studentId 学生ID
     * @return 该学生的报修列表 */
    @GetMapping("/my/{studentId}")
    public Result<List<Map<String, Object>>> getMyRepairs(@PathVariable Long studentId) {
        LambdaQueryWrapper<Repair> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Repair::getStudentId, studentId);
        wrapper.orderByDesc(Repair::getCreateTime);
        List<Repair> repairs = repairMapper.selectList(wrapper);

        List<RepairType> allTypes = repairTypeMapper.selectList(null);
        Map<Long, String> typeMap = new HashMap<>();
        for (RepairType type : allTypes) {
            typeMap.put(type.getId(), type.getTypeName());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Repair repair : repairs) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", repair.getId());
            map.put("repairNumber", repair.getRepairNumber());
            map.put("studentId", repair.getStudentId());
            map.put("roomId", repair.getRoomId());
            map.put("typeId", repair.getTypeId());
            map.put("repairTypeName", typeMap.get(repair.getTypeId()));
            map.put("title", repair.getTitle());
            map.put("description", repair.getDescription());
            map.put("images", repair.getImages());
            map.put("isEmergency", repair.getIsEmergency());
            map.put("status", repair.getStatus());
            map.put("handlerId", repair.getHandlerId());
            map.put("repairPerson", repair.getRepairPerson());
            map.put("handleRemark", repair.getHandleRemark());
            map.put("createTime", repair.getCreateTime());
            map.put("updateTime", repair.getUpdateTime());
            result.add(map);
        }

        return Result.success(result);
    }

    /** 添加报修类型
     * @param repairType 报修类型信息
     * @return 操作结果 */
    @CacheEvict(value = "repairTypeList", allEntries = true)
    @PostMapping("/type")
    public Result<Void> addType(@RequestBody RepairType repairType) {
        repairType.setStatus(Constant.STATUS_NORMAL);
        if (repairType.getSortOrder() == null) {
            repairType.setSortOrder(0);
        }
        repairTypeMapper.insert(repairType);
        return Result.success();
    }

    /** 更新报修类型
     * @param id 报修类型ID
     * @param repairType 报修类型信息
     * @return 操作结果 */
    @CacheEvict(value = "repairTypeList", allEntries = true)
    @PutMapping("/type/{id}")
    public Result<Void> updateType(@PathVariable Long id, @RequestBody RepairType repairType) {
        repairType.setId(id);
        repairTypeMapper.updateById(repairType);
        return Result.success();
    }

    /** 删除报修类型
     * @param id 报修类型ID
     * @return 操作结果 */
    @CacheEvict(value = "repairTypeList", allEntries = true)
    @DeleteMapping("/type/{id}")
    public Result<Void> deleteType(@PathVariable Long id) {
        repairTypeMapper.deleteById(id);
        return Result.success();
    }

    /** 删除报修记录
     * @param id 报修ID
     * @return 操作结果 */
    @DeleteMapping("/{id}")
    public Result<Void> deleteRepair(@PathVariable Long id) {
        repairMapper.deleteById(id);
        return Result.success();
    }
}
