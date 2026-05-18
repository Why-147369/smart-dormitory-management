package com.dormitory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.common.Result;
import com.dormitory.entity.Building;
import com.dormitory.entity.EmergencyHelp;
import com.dormitory.entity.Room;
import com.dormitory.entity.Student;
import com.dormitory.mapper.BuildingMapper;
import com.dormitory.mapper.EmergencyHelpMapper;
import com.dormitory.mapper.RoomMapper;
import com.dormitory.mapper.StudentMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * EmergencyHelpController - 紧急求助控制器
 * 提供紧急求助、故障报修等功能的RESTful接口
 * @author 王和友
 * @since 2026
 */
@RestController
@RequestMapping("/api/emergency")
public class EmergencyHelpController {

    private final EmergencyHelpMapper emergencyHelpMapper;
    private final RoomMapper roomMapper;
    private final BuildingMapper buildingMapper;
    private final StudentMapper studentMapper;

    public EmergencyHelpController(EmergencyHelpMapper emergencyHelpMapper,
                                  RoomMapper roomMapper,
                                  BuildingMapper buildingMapper,
                                  StudentMapper studentMapper) {
        this.emergencyHelpMapper = emergencyHelpMapper;
        this.roomMapper = roomMapper;
        this.buildingMapper = buildingMapper;
        this.studentMapper = studentMapper;
    }

    /**
     * 学生提交紧急求助/故障报修
     * 状态默认为0（待处理）
     * @param emergencyHelp 求助对象
     * @return 求助记录详情
     */
    @PostMapping("/help")
    public Result<EmergencyHelp> help(@RequestBody EmergencyHelp emergencyHelp) {
        emergencyHelp.setStatus(0);
        emergencyHelp.setCreateTime(LocalDateTime.now());
        emergencyHelpMapper.insert(emergencyHelp);
        return Result.success(emergencyHelp);
    }

    /**
     * 获取紧急求助记录列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param buildingId 楼栋ID（可选）
     * @param roomId 宿舍ID（可选）
     * @param status 处理状态（0-待处理，1-已接单，2-处理中，3-已解决）（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 分页后的求助记录列表
     */
    @GetMapping("/list")
    public Result<Page<Map<String, Object>>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                                   @RequestParam(required = false) Long buildingId,
                                                   @RequestParam(required = false) Long roomId,
                                                   @RequestParam(required = false) Integer status,
                                                   @RequestParam(required = false) String startDate,
                                                   @RequestParam(required = false) String endDate) {
        LambdaQueryWrapper<EmergencyHelp> wrapper = new LambdaQueryWrapper<>();
        if (roomId != null) {
            wrapper.eq(EmergencyHelp::getRoomId, roomId);
        }
        if (status != null) {
            wrapper.eq(EmergencyHelp::getStatus, status);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(EmergencyHelp::getCreateTime, LocalDateTime.parse(startDate + "T00:00:00"));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(EmergencyHelp::getCreateTime, LocalDateTime.parse(endDate + "T23:59:59"));
        }
        wrapper.orderByDesc(EmergencyHelp::getCreateTime);
        
        List<EmergencyHelp> allList = emergencyHelpMapper.selectList(wrapper);
        
        List<Map<String, Object>> filteredList = new ArrayList<>();
        for (EmergencyHelp help : allList) {
            Room room = roomMapper.selectById(help.getRoomId());
            if (room != null) {
                if (buildingId != null && !buildingId.equals(room.getBuildingId())) {
                    continue;
                }
                Map<String, Object> map = new HashMap<>();
                map.put("id", help.getId());
                map.put("studentId", help.getStudentId());
                map.put("roomId", help.getRoomId());
                map.put("content", help.getContent());
                map.put("status", help.getStatus());
                map.put("handleTime", help.getHandleTime());
                map.put("handleRemark", help.getHandleRemark());
                map.put("createTime", help.getCreateTime());
                map.put("roomNumber", room.getRoomNumber());
                
                Building building = buildingMapper.selectById(room.getBuildingId());
                if (building != null) {
                    map.put("buildingId", building.getId());
                    map.put("buildingName", building.getBuildingName());
                }
                
                Student student = studentMapper.selectById(help.getStudentId());
                if (student != null) {
                    map.put("studentName", student.getName());
                    map.put("studentNumber", student.getStudentNumber());
                }
                
                filteredList.add(map);
            }
        }
        
        int total = filteredList.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Map<String, Object>> pageList = start < total ? filteredList.subList(start, end) : new ArrayList<>();
        
        Page<Map<String, Object>> resultPage = new Page<>(pageNum, pageSize, total);
        resultPage.setRecords(pageList);
        return Result.success(resultPage);
    }

    /**
     * 根据ID获取紧急求助记录详情
     * @param id 求助记录ID
     * @return 求助记录详情
     */
    @GetMapping("/{id}")
    public Result<EmergencyHelp> getById(@PathVariable Long id) {
        EmergencyHelp emergencyHelp = emergencyHelpMapper.selectById(id);
        return Result.success(emergencyHelp);
    }

    /**
     * 接单紧急求助
     * 将状态从待处理（0）改为已接单（1）
     * @param id 求助记录ID
     * @return 操作结果
     */
    @PutMapping("/receive/{id}")
    public Result<Void> receive(@PathVariable Long id) {
        EmergencyHelp emergencyHelp = emergencyHelpMapper.selectById(id);
        emergencyHelp.setStatus(1);
        emergencyHelpMapper.updateById(emergencyHelp);
        return Result.success();
    }

    /**
     * 开始处理紧急求助
     * 将状态从已接单（1）改为处理中（2）
     * @param id 求助记录ID
     * @return 操作结果
     */
    @PutMapping("/handle/{id}")
    public Result<Void> handle(@PathVariable Long id) {
        EmergencyHelp emergencyHelp = emergencyHelpMapper.selectById(id);
        emergencyHelp.setStatus(2);
        emergencyHelpMapper.updateById(emergencyHelp);
        return Result.success();
    }

    /**
     * 解决紧急求助
     * 将状态改为已解决（3），记录处理时间和备注
     * @param id 求助记录ID
     * @param remark 处理备注（可选）
     * @return 操作结果
     */
    @PutMapping("/resolve/{id}")
    public Result<Void> resolve(@PathVariable Long id, @RequestParam(required = false) String remark) {
        EmergencyHelp emergencyHelp = emergencyHelpMapper.selectById(id);
        emergencyHelp.setStatus(3);
        emergencyHelp.setHandleTime(LocalDateTime.now());
        emergencyHelp.setHandleRemark(remark);
        emergencyHelpMapper.updateById(emergencyHelp);
        return Result.success();
    }

    /**
     * 获取学生自己的紧急求助记录
     * @param studentId 学生ID
     * @return 求助记录列表
     */
    @GetMapping("/my/{studentId}")
    public Result<List<Map<String, Object>>> getMyHelps(@PathVariable Long studentId) {
        LambdaQueryWrapper<EmergencyHelp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmergencyHelp::getStudentId, studentId);
        wrapper.orderByDesc(EmergencyHelp::getCreateTime);
        List<EmergencyHelp> list = emergencyHelpMapper.selectList(wrapper);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (EmergencyHelp help : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", help.getId());
            map.put("studentId", help.getStudentId());
            map.put("roomId", help.getRoomId());
            map.put("content", help.getContent());
            map.put("status", help.getStatus());
            map.put("handleTime", help.getHandleTime());
            map.put("handleRemark", help.getHandleRemark());
            map.put("createTime", help.getCreateTime());
            
            Room room = roomMapper.selectById(help.getRoomId());
            if (room != null) {
                map.put("roomNumber", room.getRoomNumber());
                Building building = buildingMapper.selectById(room.getBuildingId());
                if (building != null) {
                    map.put("buildingName", building.getBuildingName());
                }
            }
            result.add(map);
        }
        return Result.success(result);
    }
}
