package com.dormitory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.common.Result;
import com.dormitory.entity.Visitor;
import com.dormitory.entity.Student;
import com.dormitory.entity.Room;
import com.dormitory.entity.Building;
import com.dormitory.mapper.VisitorMapper;
import com.dormitory.mapper.StudentMapper;
import com.dormitory.mapper.RoomMapper;
import com.dormitory.mapper.BuildingMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * VisitorController - 访客管理控制器，处理访客预约、审批、登记等业务功能
 * @author 王和友
 * @since 2026
 */
@RestController
@RequestMapping("/api/visitor")
public class VisitorController {

    private final VisitorMapper visitorMapper;
    private final StudentMapper studentMapper;
    private final RoomMapper roomMapper;
    private final BuildingMapper buildingMapper;

    public VisitorController(VisitorMapper visitorMapper, StudentMapper studentMapper,
                           RoomMapper roomMapper, BuildingMapper buildingMapper) {
        this.visitorMapper = visitorMapper;
        this.studentMapper = studentMapper;
        this.roomMapper = roomMapper;
        this.buildingMapper = buildingMapper;
    }

    /**
     * 申请访客预约 - 学生为访客提交预约申请
     * @param visitor 访客预约信息，包含访客姓名、联系方式、访问时间、来访原因等
     * @return 返回创建的访客预约记录
     */
    @PostMapping("/apply")
    public Result<Visitor> apply(@RequestBody Visitor visitor) {
        visitor.setStatus(0);
        visitor.setCreateTime(LocalDateTime.now());
        
        if (visitor.getVisitTime() != null) {
            try {
                String timeStr = visitor.getVisitTime().toString();
                if (timeStr.contains(" ")) {
                    visitor.setVisitTime(java.time.LocalDateTime.parse(timeStr.replace(" ", "T")));
                }
            } catch (Exception e) {
                try {
                    visitor.setVisitTime(java.time.LocalDateTime.of(
                        java.time.LocalDate.parse(visitor.getVisitTime().toString().substring(0, 10)),
                        java.time.LocalTime.of(0, 0, 0)
                    ));
                } catch (Exception ex) {
                }
            }
        }
        
        visitorMapper.insert(visitor);
        return Result.success(visitor);
    }

    /**
     * 查询访客预约列表 - 管理员查看所有访客预约，支持按楼栋、学生ID、状态筛选
     * @param pageNum 页码，默认1
     * @param pageSize 每页数量，默认10
     * @param buildingId 楼栋ID（可选），用于筛选特定楼栋的预约
     * @param studentId 学生ID（可选），用于筛选特定学生的预约
     * @param status 状态（可选），0-待审批 1-已通过 2-已拒绝 3-已完成 4-已取消
     * @return 返回分页的访客预约列表，包含学生、宿舍等详细信息
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
        Map<Long, Room> roomMap = new HashMap<>();
        List<Building> buildings = buildingMapper.selectList(null);
        for (Building b : buildings) {
            buildingMap.put(b.getId(), b);
        }
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

        Page<Visitor> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Visitor> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) {
            wrapper.eq(Visitor::getStudentId, studentId);
        }
        if (status != null) {
            wrapper.eq(Visitor::getStatus, status);
        }
        if (filteredStudentIds != null && !filteredStudentIds.isEmpty()) {
            wrapper.in(Visitor::getStudentId, filteredStudentIds);
        }
        wrapper.orderByDesc(Visitor::getCreateTime);
        Page<Visitor> result = visitorMapper.selectPage(page, wrapper);

        List<Map<String, Object>> records = new ArrayList<>();
        for (Visitor visitor : result.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", visitor.getId());
            map.put("studentId", visitor.getStudentId());
            map.put("visitorName", visitor.getVisitorName());
            map.put("gender", visitor.getGender());
            map.put("phone", visitor.getPhone());
            map.put("source", visitor.getSource());
            map.put("visitTime", visitor.getVisitTime());
            map.put("purpose", visitor.getPurpose());
            map.put("status", visitor.getStatus());
            map.put("rejectReason", visitor.getRejectReason());
            map.put("createTime", visitor.getCreateTime());

            Student student = studentMap.get(visitor.getStudentId());
            if (student != null) {
                map.put("studentName", student.getName());
                map.put("studentNumber", student.getStudentNumber());

                Room room = student.getRoomId() != null ? roomMap.get(student.getRoomId()) : null;
                if (room != null) {
                    map.put("roomNumber", room.getRoomNumber());
                    Building building = room.getBuildingId() != null ? buildingMap.get(room.getBuildingId()) : null;
                    if (building != null) {
                        map.put("buildingName", building.getBuildingName());
                        map.put("buildingId", building.getId());
                    }
                }
            }
            records.add(map);
        }

        Page<Map<String, Object>> resultPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        resultPage.setRecords(records);
        return Result.success(resultPage);
    }

    /**
     * 根据ID查询访客预约 - 获取指定访客预约的详细信息
     * @param id 访客预约ID
     * @return 返回访客预约详情
     */
    @GetMapping("/{id}")
    public Result<Visitor> getById(@PathVariable Long id) {
        Visitor visitor = visitorMapper.selectById(id);
        return Result.success(visitor);
    }

    /**
     * 查询我的访客预约 - 学生查看自己的访客预约记录
     * @param studentId 学生ID
     * @return 返回该学生的所有访客预约记录
     */
    @GetMapping("/my/{studentId}")
    public Result<List<Map<String, Object>>> getMyVisitors(@PathVariable Long studentId) {
        LambdaQueryWrapper<Visitor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Visitor::getStudentId, studentId);
        wrapper.orderByDesc(Visitor::getCreateTime);
        List<Visitor> visitors = visitorMapper.selectList(wrapper);

        List<Map<String, Object>> records = new ArrayList<>();
        for (Visitor visitor : visitors) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", visitor.getId());
            map.put("visitorName", visitor.getVisitorName());
            map.put("gender", visitor.getGender());
            map.put("phone", visitor.getPhone());
            map.put("source", visitor.getSource());
            map.put("visitTime", visitor.getVisitTime());
            map.put("purpose", visitor.getPurpose());
            map.put("status", visitor.getStatus());
            map.put("rejectReason", visitor.getRejectReason());
            map.put("createTime", visitor.getCreateTime());
            records.add(map);
        }
        return Result.success(records);
    }

    /**
     * 取消访客预约 - 学生取消自己提交的待审批访客预约
     * @param id 访客预约ID
     * @return 返回操作结果
     */
    @PutMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        Visitor visitor = visitorMapper.selectById(id);
        if (visitor.getStatus() != 0) {
            return Result.error("只有待审批的预约可以取消");
        }
        visitor.setStatus(4);
        visitorMapper.updateById(visitor);
        return Result.success();
    }

    /**
     * 审批通过访客预约 - 管理员批准访客预约申请
     * @param id 访客预约ID
     * @return 返回操作结果
     */
    @PutMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id) {
        Visitor visitor = visitorMapper.selectById(id);
        visitor.setStatus(1);
        visitor.setApproverId(1L);
        visitorMapper.updateById(visitor);
        return Result.success();
    }

    /**
     * 审批拒绝访客预约 - 管理员拒绝访客预约申请
     * @param id 访客预约ID
     * @param reason 拒绝原因
     * @return 返回操作结果
     */
    @PutMapping("/reject/{id}")
    public Result<Void> reject(@PathVariable Long id, @RequestParam String reason) {
        Visitor visitor = visitorMapper.selectById(id);
        visitor.setStatus(2);
        visitor.setRejectReason(reason);
        visitorMapper.updateById(visitor);
        return Result.success();
    }

    /**
     * 完成访客预约 - 标记访客已离开，完成访问
     * @param id 访客预约ID
     * @return 返回操作结果
     */
    @PutMapping("/complete/{id}")
    public Result<Void> complete(@PathVariable Long id) {
        Visitor visitor = visitorMapper.selectById(id);
        visitor.setStatus(3);
        visitorMapper.updateById(visitor);
        return Result.success();
    }

    /**
     * 删除访客预约 - 管理员删除指定的访客预约记录
     * @param id 访客预约ID
     * @return 返回操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        visitorMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除访客预约 - 管理员批量删除指定的访客预约记录
     * @param ids 访客预约ID列表
     * @return 返回操作结果
     */
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        visitorMapper.deleteBatchIds(ids);
        return Result.success();
    }
}
