package com.dormitory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.common.Result;
import com.dormitory.entity.*;
import com.dormitory.mapper.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RoomChangeController - 换寝管理控制器，处理学生换寝申请、审批、查询等业务功能
 * @author 王和友
 * @since 2026
 */
@RestController
@RequestMapping("/api/room/change")
public class RoomChangeController {

    private final RoomChangeMapper roomChangeMapper;
    private final StudentMapper studentMapper;
    private final RoomMapper roomMapper;
    private final BedMapper bedMapper;
    private final BuildingMapper buildingMapper;
    private final MessageMapper messageMapper;

    public RoomChangeController(RoomChangeMapper roomChangeMapper, StudentMapper studentMapper,
                              RoomMapper roomMapper, BedMapper bedMapper, BuildingMapper buildingMapper,
                              MessageMapper messageMapper) {
        this.roomChangeMapper = roomChangeMapper;
        this.studentMapper = studentMapper;
        this.roomMapper = roomMapper;
        this.bedMapper = bedMapper;
        this.buildingMapper = buildingMapper;
        this.messageMapper = messageMapper;
    }

    /**
     * 申请换寝 - 学生提交换寝申请
     * @param roomChange 换寝申请信息，包含目标宿舍、床位及换寝原因
     * @return 返回创建的换寝申请记录
     */
    @PostMapping("/apply")
    public Result<RoomChange> apply(@RequestBody RoomChange roomChange) {
        Student student = studentMapper.selectById(roomChange.getStudentId());
        if (student == null || student.getRoomId() == null) {
            return Result.error("学生未分配宿舍，无法申请换寝");
        }
        
        roomChange.setCurrentRoomId(student.getRoomId());
        
        LambdaQueryWrapper<Bed> bedWrapper = new LambdaQueryWrapper<>();
        bedWrapper.eq(Bed::getRoomId, student.getRoomId());
        bedWrapper.eq(Bed::getStudentId, student.getId());
        Bed currentBed = bedMapper.selectOne(bedWrapper);
        if (currentBed != null) {
            roomChange.setCurrentBedId(currentBed.getId());
        } else {
            roomChange.setCurrentBedId(null);
        }
        
        roomChange.setStatus(0);
        roomChange.setCreateTime(LocalDateTime.now());
        roomChangeMapper.insert(roomChange);
        return Result.success(roomChange);
    }

    /**
     * 查询换寝申请列表 - 管理员查看所有换寝申请，支持按楼栋、学生ID、状态筛选
     * @param pageNum 页码，默认1
     * @param pageSize 每页数量，默认10
     * @param buildingId 楼栋ID（可选），用于筛选特定楼栋的申请
     * @param studentId 学生ID（可选），用于筛选特定学生的申请
     * @param status 状态（可选），0-待审批 1-已通过 2-已拒绝
     * @return 返回分页的换寝申请列表，包含学生、宿舍、床位等详细信息
     */
    @GetMapping("/list")
    public Result<Page<Map<String, Object>>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                                 @RequestParam(required = false) Long buildingId,
                                                 @RequestParam(required = false) Long studentId,
                                                 @RequestParam(required = false) Integer status) {
        List<Student> allStudents = studentMapper.selectList(null);
        Map<Long, Student> studentMap = new HashMap<>();
        Set<Long> studentIds = new HashSet<>();
        for (Student s : allStudents) {
            studentMap.put(s.getId(), s);
            studentIds.add(s.getId());
        }

        Map<Long, Building> buildingMap = new HashMap<>();
        List<Building> buildings = buildingMapper.selectList(null);
        for (Building b : buildings) {
            buildingMap.put(b.getId(), b);
        }

        Map<Long, Room> roomMap = new HashMap<>();
        List<Room> rooms = roomMapper.selectList(null);
        for (Room r : rooms) {
            roomMap.put(r.getId(), r);
        }

        Set<Long> filteredStudentIds = studentIds;
        if (buildingId != null) {
            filteredStudentIds = new HashSet<>();
            for (Student s : allStudents) {
                if (s.getBuildingId() != null && s.getBuildingId().equals(buildingId)) {
                    filteredStudentIds.add(s.getId());
                }
            }
        }

        if (studentId != null) {
            filteredStudentIds = new HashSet<>();
            filteredStudentIds.add(studentId);
        }

        Page<RoomChange> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<RoomChange> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(RoomChange::getStatus, status);
        }
        if (filteredStudentIds != null && !filteredStudentIds.isEmpty()) {
            wrapper.in(RoomChange::getStudentId, filteredStudentIds);
        }
        wrapper.orderByDesc(RoomChange::getCreateTime);
        Page<RoomChange> result = roomChangeMapper.selectPage(page, wrapper);

        List<Map<String, Object>> records = new ArrayList<>();
        for (RoomChange rc : result.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", rc.getId());
            map.put("studentId", rc.getStudentId());
            map.put("currentRoomId", rc.getCurrentRoomId());
            map.put("currentBedId", rc.getCurrentBedId());
            map.put("targetRoomId", rc.getTargetRoomId());
            map.put("targetBedId", rc.getTargetBedId());
            map.put("reason", rc.getReason());
            map.put("status", rc.getStatus());
            map.put("rejectReason", rc.getRejectReason());
            map.put("createTime", rc.getCreateTime());

            Student student = studentMap.get(rc.getStudentId());
            if (student != null) {
                map.put("studentName", student.getName());
                map.put("studentNumber", student.getStudentNumber());

                Room currentRoom = roomMap.get(rc.getCurrentRoomId());
                if (currentRoom != null) {
                    map.put("currentRoomNumber", currentRoom.getRoomNumber());
                    Building currentBuilding = buildingMap.get(currentRoom.getBuildingId());
                    if (currentBuilding != null) {
                        map.put("currentBuildingName", currentBuilding.getBuildingName());
                        map.put("currentBuildingId", currentBuilding.getId());
                    }
                }

                Room targetRoom = roomMap.get(rc.getTargetRoomId());
                if (targetRoom != null) {
                    map.put("targetRoomNumber", targetRoom.getRoomNumber());
                    Building targetBuilding = buildingMap.get(targetRoom.getBuildingId());
                    if (targetBuilding != null) {
                        map.put("targetBuildingName", targetBuilding.getBuildingName());
                        map.put("targetBuildingId", targetBuilding.getId());
                    }
                }

                Bed currentBed = bedMapper.selectById(rc.getCurrentBedId());
                if (currentBed != null) {
                    map.put("currentBedNumber", currentBed.getBedNumber());
                }

                Bed targetBed = bedMapper.selectById(rc.getTargetBedId());
                if (targetBed != null) {
                    map.put("targetBedNumber", targetBed.getBedNumber());
                }
            }

            records.add(map);
        }

        Page<Map<String, Object>> resultPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        resultPage.setRecords(records);
        return Result.success(resultPage);
    }

    /**
     * 查询我的换寝申请 - 学生查看自己提交的换寝申请记录
     * @param studentId 学生ID
     * @return 返回该学生的所有换寝申请记录
     */
    @GetMapping("/my/{studentId}")
    public Result<List<Map<String, Object>>> getMyApplications(@PathVariable Long studentId) {
        LambdaQueryWrapper<RoomChange> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomChange::getStudentId, studentId);
        wrapper.orderByDesc(RoomChange::getCreateTime);
        List<RoomChange> list = roomChangeMapper.selectList(wrapper);

        Map<Long, Room> roomMap = new HashMap<>();
        List<Room> rooms = roomMapper.selectList(null);
        for (Room r : rooms) {
            roomMap.put(r.getId(), r);
        }

        Map<Long, Bed> bedMap = new HashMap<>();
        List<Bed> beds = bedMapper.selectList(null);
        for (Bed b : beds) {
            bedMap.put(b.getId(), b);
        }

        List<Map<String, Object>> records = new ArrayList<>();
        for (RoomChange rc : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", rc.getId());
            map.put("currentRoomId", rc.getCurrentRoomId());
            map.put("currentBedId", rc.getCurrentBedId());
            map.put("targetRoomId", rc.getTargetRoomId());
            map.put("targetBedId", rc.getTargetBedId());
            map.put("reason", rc.getReason());
            map.put("status", rc.getStatus());
            map.put("rejectReason", rc.getRejectReason());
            map.put("createTime", rc.getCreateTime());

            Room currentRoom = roomMap.get(rc.getCurrentRoomId());
            if (currentRoom != null) {
                map.put("currentRoomNumber", currentRoom.getRoomNumber());
            }

            Room targetRoom = roomMap.get(rc.getTargetRoomId());
            if (targetRoom != null) {
                map.put("targetRoomNumber", targetRoom.getRoomNumber());
            }

            Bed currentBed = bedMap.get(rc.getCurrentBedId());
            if (currentBed != null) {
                map.put("currentBedNumber", currentBed.getBedNumber());
            }

            Bed targetBed = bedMap.get(rc.getTargetBedId());
            if (targetBed != null) {
                map.put("targetBedNumber", targetBed.getBedNumber());
            }

            records.add(map);
        }
        return Result.success(records);
    }

    /**
     * 审批通过换寝申请 - 管理员批准学生的换寝申请，自动调整宿舍床位分配并发送通知
     * @param id 换寝申请ID
     * @return 返回换寝后的宿舍信息（楼栋、房间、床位号）
     */
    @PutMapping("/approve/{id}")
    public Result<Map<String, Object>> approve(@PathVariable Long id) {
        RoomChange roomChange = roomChangeMapper.selectById(id);
        if (roomChange.getStatus() != 0) {
            return Result.error("该申请已被处理");
        }

        Student student = studentMapper.selectById(roomChange.getStudentId());
        if (student == null) {
            return Result.error("学生不存在");
        }

        Long oldRoomId = roomChange.getCurrentRoomId();
        Long oldBedId = roomChange.getCurrentBedId();
        
        if (oldBedId != null && oldRoomId != null) {
            Bed checkBed = bedMapper.selectById(oldBedId);
            if (checkBed == null || checkBed.getRoomId() == null || !checkBed.getRoomId().equals(oldRoomId)) {
                LambdaQueryWrapper<Bed> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Bed::getRoomId, oldRoomId);
                wrapper.eq(Bed::getStudentId, student.getId());
                Bed actualBed = bedMapper.selectOne(wrapper);
                if (actualBed != null) {
                    oldBedId = actualBed.getId();
                }
            }
        }
        
        if (oldBedId != null) {
            Bed oldBed = bedMapper.selectById(oldBedId);
            if (oldBed != null) {
                LambdaUpdateWrapper<Bed> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(Bed::getId, oldBedId);
                updateWrapper.set(Bed::getStatus, 0);
                updateWrapper.set(Bed::getStudentId, null);
                bedMapper.update(null, updateWrapper);
            }
            
            if (oldRoomId != null) {
                Room oldRoom = roomMapper.selectById(oldRoomId);
                if (oldRoom != null && oldRoom.getCurrentCount() != null && oldRoom.getCurrentCount() > 0) {
                    oldRoom.setCurrentCount(oldRoom.getCurrentCount() - 1);
                    roomMapper.updateById(oldRoom);
                }
            }
        }

        Long newBedId = roomChange.getTargetBedId();
        Long newRoomId = roomChange.getTargetRoomId();
        
        Room newRoom = roomMapper.selectById(newRoomId);
        if (newRoom == null) {
            return Result.error("目标宿舍不存在");
        }

        Bed newBed = bedMapper.selectById(newBedId);
        if (newBed == null) {
            return Result.error("目标床位不存在");
        }

        if (newBed.getStatus() != 0) {
            return Result.error("目标床位不可用");
        }

        newBed.setStatus(1);
        LambdaUpdateWrapper<Bed> newBedWrapper = new LambdaUpdateWrapper<>();
        newBedWrapper.eq(Bed::getId, newBedId);
        newBedWrapper.set(Bed::getStatus, 1);
        newBedWrapper.set(Bed::getStudentId, student.getId());
        bedMapper.update(null, newBedWrapper);

        newRoom.setCurrentCount((newRoom.getCurrentCount() == null ? 0 : newRoom.getCurrentCount()) + 1);
        roomMapper.updateById(newRoom);

        student.setBuildingId(newRoom.getBuildingId());
        student.setRoomId(newRoomId);
        student.setBedNumber(newBed.getBedNumber());
        studentMapper.updateById(student);

        roomChange.setStatus(1);
        roomChange.setApproveTime(LocalDateTime.now());
        roomChangeMapper.updateById(roomChange);

        // 发送消息通知学生
        Message message = new Message();
        message.setUserId(student.getId());
        message.setUserType(1); // 1=学生
        message.setTitle("换寝申请");
        message.setContent("你的宿舍已更换到" + newRoom.getRoomNumber() + "宿舍" + newBed.getBedNumber() + "号床");
        message.setMessageType("换寝通知");
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);

        Map<String, Object> result = new HashMap<>();
        result.put("buildingId", newRoom.getBuildingId());
        result.put("roomId", newRoomId);
        result.put("roomNumber", newRoom.getRoomNumber());
        result.put("bedNumber", newBed.getBedNumber());
        
        Building building = buildingMapper.selectById(newRoom.getBuildingId());
        if (building != null) {
            result.put("buildingName", building.getBuildingName());
        }
        
        return Result.success(result);
    }

    /**
     * 审批拒绝换寝申请 - 管理员拒绝学生的换寝申请
     * @param id 换寝申请ID
     * @param reason 拒绝原因
     * @return 返回操作结果
     */
    @PutMapping("/reject/{id}")
    public Result<Void> reject(@PathVariable Long id, @RequestParam String reason) {
        RoomChange roomChange = roomChangeMapper.selectById(id);
        if (roomChange.getStatus() != 0) {
            return Result.error("该申请已被处理");
        }
        roomChange.setStatus(2);
        roomChange.setRejectReason(reason);
        roomChange.setApproveTime(LocalDateTime.now());
        roomChangeMapper.updateById(roomChange);

        // 发送消息通知学生
        Student student = studentMapper.selectById(roomChange.getStudentId());
        if (student != null) {
            Message message = new Message();
            message.setUserId(student.getId());
            message.setUserType(1); // 1=学生
            message.setTitle("换寝申请");
            message.setContent("你的换寝申请被拒绝，请查看原因");
            message.setMessageType("换寝通知");
            message.setIsRead(0);
            message.setCreateTime(LocalDateTime.now());
            messageMapper.insert(message);
        }
        return Result.success();
    }

    /**
     * 删除换寝申请 - 管理员删除指定的换寝申请记录
     * @param id 换寝申请ID
     * @return 返回操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roomChangeMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除换寝申请 - 管理员批量删除指定的换寝申请记录
     * @param ids 换寝申请ID列表
     * @return 返回操作结果
     */
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        roomChangeMapper.deleteBatchIds(ids);
        return Result.success();
    }
}
