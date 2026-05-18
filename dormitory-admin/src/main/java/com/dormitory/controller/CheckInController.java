package com.dormitory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.common.Constant;
import com.dormitory.common.Result;
import com.dormitory.entity.CheckIn;
import com.dormitory.entity.CheckInApply;
import com.dormitory.entity.Student;
import com.dormitory.entity.Building;
import com.dormitory.entity.Room;
import com.dormitory.mapper.CheckInApplyMapper;
import com.dormitory.mapper.CheckInMapper;
import com.dormitory.mapper.StudentMapper;
import com.dormitory.mapper.BuildingMapper;
import com.dormitory.mapper.RoomMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * CheckInController - 入住管理控制器
 * 提供学生入住打卡、补卡申请、申请审批等功能
 * @author 王和友
 * @since 2026
 */
@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    private final CheckInMapper checkInMapper;
    private final CheckInApplyMapper checkInApplyMapper;
    private final StudentMapper studentMapper;
    private final BuildingMapper buildingMapper;
    private final RoomMapper roomMapper;

    public CheckInController(CheckInMapper checkInMapper, CheckInApplyMapper checkInApplyMapper, 
                            StudentMapper studentMapper, BuildingMapper buildingMapper, RoomMapper roomMapper) {
        this.checkInMapper = checkInMapper;
        this.checkInApplyMapper = checkInApplyMapper;
        this.studentMapper = studentMapper;
        this.buildingMapper = buildingMapper;
        this.roomMapper = roomMapper;
    }

    /**
     * 学生入住打卡
     * 记录学生每日的入住打卡信息，自动判断是否迟到（23:00后打卡算迟到）
     * @param studentId 学生ID
     * @return 打卡结果
     */
    @PostMapping("/record")
    public Result<CheckIn> record(@RequestParam Long studentId) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        LambdaQueryWrapper<CheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckIn::getStudentId, studentId);
        wrapper.eq(CheckIn::getCheckDate, today);
        CheckIn existCheckIn = checkInMapper.selectOne(wrapper);

        if (existCheckIn != null) {
            return Result.error("今日已打卡");
        }

        CheckIn checkIn = new CheckIn();
        checkIn.setStudentId(studentId);
        checkIn.setCheckDate(today);
        checkIn.setCheckTime(now);
        checkIn.setStatus(Constant.CHECK_IN_STATUS_NORMAL);

        boolean isLate = now.isAfter(LocalTime.of(23, 0));
        checkIn.setIsLate(isLate ? 1 : 0);

        checkInMapper.insert(checkIn);
        return Result.success(checkIn);
    }

    /**
     * 获取学生今日打卡状态
     * 查询指定学生今日是否已完成打卡
     * @param studentId 学生ID
     * @return 打卡状态信息
     */
    @GetMapping("/status/{studentId}")
    public Result<Map<String, Object>> getStatus(@PathVariable Long studentId) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<CheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckIn::getStudentId, studentId);
        wrapper.eq(CheckIn::getCheckDate, today);
        CheckIn checkIn = checkInMapper.selectOne(wrapper);
        
        Map<String, Object> result = new HashMap<>();
        if (checkIn != null) {
            result.put("id", checkIn.getId());
            result.put("studentId", checkIn.getStudentId());
            result.put("checkDate", checkIn.getCheckDate());
            result.put("checkInTime", checkIn.getCheckTime() != null ? checkIn.getCheckTime().toString() : "");
            result.put("status", checkIn.getStatus());
            result.put("isLate", checkIn.getIsLate());
        }
        return Result.success(result);
    }

    /**
     * 获取学生打卡历史记录
     * 查询指定学生的所有打卡历史记录，支持分页
     * @param studentId 学生ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 打卡历史分页数据
     */
    @GetMapping("/history/{studentId}")
    public Result<Page<Map<String, Object>>> history(@PathVariable Long studentId,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<CheckIn> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckIn::getStudentId, studentId);
        wrapper.orderByDesc(CheckIn::getCheckDate);
        Page<CheckIn> result = checkInMapper.selectPage(page, wrapper);
        
        Page<Map<String, Object>> resultPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<Map<String, Object>> records = new ArrayList<>();
        for (CheckIn checkIn : result.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", checkIn.getId());
            map.put("studentId", checkIn.getStudentId());
            map.put("checkInDate", checkIn.getCheckDate() != null ? checkIn.getCheckDate().toString() : "");
            map.put("checkInTime", checkIn.getCheckTime() != null ? checkIn.getCheckTime().toString() : "");
            map.put("status", checkIn.getStatus());
            map.put("isLate", checkIn.getIsLate());
            records.add(map);
        }
        resultPage.setRecords(records);
        return Result.success(resultPage);
    }

    /**
     * 学生申请补卡
     * 学生因特殊原因无法打卡时，可提交补卡申请
     * @param apply 补卡申请信息
     * @return 操作结果
     */
    @PostMapping("/apply")
    public Result<Void> apply(@RequestBody CheckInApply apply) {
        apply.setStatus(Constant.CHECK_IN_STATUS_SUPPLEMENT);
        apply.setCreateTime(LocalDateTime.now());
        checkInApplyMapper.insert(apply);
        return Result.success();
    }

    /**
     * 获取学生自己的补卡申请列表
     * 学生查看自己提交的补卡申请记录
     * @param studentId 学生ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 补卡申请分页数据
     */
    @GetMapping("/apply/my")
    public Result<Page<Map<String, Object>>> myApplyList(@RequestParam Long studentId,
                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<CheckInApply> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CheckInApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckInApply::getStudentId, studentId);
        wrapper.orderByDesc(CheckInApply::getCreateTime);
        Page<CheckInApply> result = checkInApplyMapper.selectPage(page, wrapper);
        
        List<Map<String, Object>> records = new ArrayList<>();
        for (CheckInApply apply : result.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", apply.getId());
            map.put("applyDate", apply.getApplyDate() != null ? apply.getApplyDate().toString() : "");
            map.put("reason", apply.getReason());
            map.put("status", apply.getStatus());
            map.put("createTime", apply.getCreateTime());
            map.put("rejectReason", apply.getRejectReason());
            records.add(map);
        }
        
        Page<Map<String, Object>> resultPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        resultPage.setRecords(records);
        return Result.success(resultPage);
    }

    /**
     * 获取所有补卡申请列表（管理员）
     * 管理员查看所有待审批的补卡申请
     * @param managerId 管理员ID（可选）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 补卡申请分页数据
     */
    @GetMapping("/apply/list")
    public Result<Page<Map<String, Object>>> applyList(@RequestParam(required = false) Long managerId,
                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<CheckInApply> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CheckInApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckInApply::getStatus, Constant.CHECK_IN_STATUS_SUPPLEMENT);
        wrapper.orderByDesc(CheckInApply::getCreateTime);
        Page<CheckInApply> result = checkInApplyMapper.selectPage(page, wrapper);

        Set<Long> studentIds = new HashSet<>();
        for (CheckInApply apply : result.getRecords()) {
            if (apply.getStudentId() != null) {
                studentIds.add(apply.getStudentId());
            }
        }

        Map<Long, Student> studentMap = new HashMap<>();
        if (!studentIds.isEmpty()) {
            List<Student> students = studentMapper.selectBatchIds(studentIds);
            for (Student s : students) {
                studentMap.put(s.getId(), s);
            }
        }

        List<Map<String, Object>> records = new ArrayList<>();
        for (CheckInApply apply : result.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", apply.getId());
            map.put("studentId", apply.getStudentId());
            map.put("applyDate", apply.getApplyDate() != null ? apply.getApplyDate().toString() : "");
            map.put("reason", apply.getReason());
            map.put("status", apply.getStatus());
            map.put("createTime", apply.getCreateTime());

            Student student = studentMap.get(apply.getStudentId());
            if (student != null) {
                map.put("studentNumber", student.getStudentNumber());
                map.put("studentName", student.getName());
            }
            records.add(map);
        }

        Page<Map<String, Object>> resultPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        resultPage.setRecords(records);
        return Result.success(resultPage);
    }

    /**
     * 审批通过补卡申请
     * 管理员批准学生的补卡申请，自动生成补卡记录
     * @param id 补卡申请ID
     * @return 操作结果
     */
    @PutMapping("/apply/approve/{id}")
    public Result<Void> approveApply(@PathVariable Long id) {
        CheckInApply apply = checkInApplyMapper.selectById(id);
        
        LambdaQueryWrapper<CheckIn> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(CheckIn::getStudentId, apply.getStudentId());
        checkWrapper.eq(CheckIn::getCheckDate, apply.getApplyDate());
        CheckIn existCheckIn = checkInMapper.selectOne(checkWrapper);
        
        if (existCheckIn != null) {
            apply.setStatus(1);
            apply.setManagerId(1L);
            apply.setUpdateTime(LocalDateTime.now());
            checkInApplyMapper.updateById(apply);
            return Result.success();
        }
        
        apply.setStatus(1);
        apply.setManagerId(1L);
        apply.setUpdateTime(LocalDateTime.now());
        checkInApplyMapper.updateById(apply);

        CheckIn checkIn = new CheckIn();
        checkIn.setStudentId(apply.getStudentId());
        checkIn.setCheckDate(apply.getApplyDate());
        checkIn.setCheckTime(LocalTime.of(22, 30));
        checkIn.setStatus(Constant.CHECK_IN_STATUS_SUPPLEMENT);
        checkIn.setIsLate(1);
        checkIn.setRemark("补卡");
        checkInMapper.insert(checkIn);

        return Result.success();
    }

    /**
     * 拒绝补卡申请
     * 管理员拒绝学生的补卡申请
     * @param id 补卡申请ID
     * @param reason 拒绝原因
     * @return 操作结果
     */
    @PutMapping("/apply/reject/{id}")
    public Result<Void> rejectApply(@PathVariable Long id, @RequestParam String reason) {
        CheckInApply apply = checkInApplyMapper.selectById(id);
        apply.setStatus(2);
        apply.setRejectReason(reason);
        apply.setUpdateTime(LocalDateTime.now());
        checkInApplyMapper.updateById(apply);
        return Result.success();
    }

    /**
     * 获取打卡记录列表（管理员）
     * 管理员查看所有学生的打卡记录，支持按楼栋和日期筛选
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param buildingId 楼栋ID（可选）
     * @param checkDate 打卡日期（可选）
     * @return 打卡记录分页数据
     */
    @GetMapping("/list")
    public Result<Page<Map<String, Object>>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @RequestParam(required = false) Long buildingId,
                                       @RequestParam(required = false) String checkDate) {
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        if (buildingId != null) {
            studentWrapper.eq(Student::getBuildingId, buildingId);
        }
        List<Student> students = studentMapper.selectList(studentWrapper);
        Map<Long, Student> studentMap = new HashMap<>();
        Set<Long> studentIds = new HashSet<>();
        for (Student s : students) {
            studentMap.put(s.getId(), s);
            studentIds.add(s.getId());
        }

        Set<Long> buildingIds = new HashSet<>();
        Set<Long> roomIds = new HashSet<>();
        for (Student s : students) {
            if (s.getBuildingId() != null) {
                buildingIds.add(s.getBuildingId());
            }
            if (s.getRoomId() != null) {
                roomIds.add(s.getRoomId());
            }
        }

        Map<Long, Building> buildingMap = new HashMap<>();
        Map<Long, Room> roomMap = new HashMap<>();
        if (!buildingIds.isEmpty()) {
            List<Building> buildings = buildingMapper.selectBatchIds(buildingIds);
            for (Building b : buildings) {
                buildingMap.put(b.getId(), b);
            }
        }
        if (!roomIds.isEmpty()) {
            List<Room> rooms = roomMapper.selectBatchIds(roomIds);
            for (Room r : rooms) {
                roomMap.put(r.getId(), r);
            }
        }

        Set<Long> filteredStudentIds = studentIds;

        Page<CheckIn> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CheckIn> wrapper = new LambdaQueryWrapper<>();
        if (checkDate != null && !checkDate.isEmpty()) {
            wrapper.eq(CheckIn::getCheckDate, LocalDate.parse(checkDate));
        }
        if (filteredStudentIds != null && !filteredStudentIds.isEmpty()) {
            wrapper.in(CheckIn::getStudentId, filteredStudentIds);
        }
        wrapper.orderByDesc(CheckIn::getCheckDate, CheckIn::getCheckTime);
        Page<CheckIn> result = checkInMapper.selectPage(page, wrapper);
        
        List<Map<String, Object>> records = new ArrayList<>();
        for (CheckIn checkIn : result.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", checkIn.getId());
            map.put("studentId", checkIn.getStudentId());
            map.put("checkDate", checkIn.getCheckDate() != null ? checkIn.getCheckDate().toString() : "");
            map.put("checkTime", checkIn.getCheckTime() != null ? checkIn.getCheckTime().toString() : "");
            map.put("status", checkIn.getStatus());
            map.put("isLate", checkIn.getIsLate());
            
            Student student = studentMap.get(checkIn.getStudentId());
            if (student != null) {
                map.put("studentNumber", student.getStudentNumber());
                map.put("studentName", student.getName());
                
                Building building = student.getBuildingId() != null ? buildingMap.get(student.getBuildingId()) : null;
                if (building != null) {
                    map.put("buildingName", building.getBuildingName());
                }
                
                Room room = student.getRoomId() != null ? roomMap.get(student.getRoomId()) : null;
                if (room != null) {
                    map.put("roomNumber", room.getRoomNumber());
                }
            }
            records.add(map);
        }
        
        Page<Map<String, Object>> resultPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        resultPage.setRecords(records);
        return Result.success(resultPage);
    }

    /**
     * 导出打卡记录为Excel
     */
    @GetMapping("/export")
    public void export(@RequestParam(required = false) Long buildingId,
                       @RequestParam(required = false) String checkDate,
                       HttpServletResponse response) throws IOException {
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        if (buildingId != null) studentWrapper.eq(Student::getBuildingId, buildingId);
        List<Student> students = studentMapper.selectList(studentWrapper);
        Map<Long, Student> studentMap = new HashMap<>();
        Set<Long> studentIds = new HashSet<>();
        for (Student s : students) { studentMap.put(s.getId(), s); studentIds.add(s.getId()); }

        Set<Long> buildingIds = new HashSet<>(), roomIds = new HashSet<>();
        for (Student s : students) {
            if (s.getBuildingId() != null) buildingIds.add(s.getBuildingId());
            if (s.getRoomId() != null) roomIds.add(s.getRoomId());
        }
        Map<Long, Building> buildingMap = new HashMap<>();
        Map<Long, Room> roomMap = new HashMap<>();
        if (!buildingIds.isEmpty())
            for (Building b : buildingMapper.selectBatchIds(buildingIds)) buildingMap.put(b.getId(), b);
        if (!roomIds.isEmpty())
            for (Room r : roomMapper.selectBatchIds(roomIds)) roomMap.put(r.getId(), r);

        LambdaQueryWrapper<CheckIn> wrapper = new LambdaQueryWrapper<>();
        if (checkDate != null && !checkDate.isEmpty()) wrapper.eq(CheckIn::getCheckDate, LocalDate.parse(checkDate));
        if (!studentIds.isEmpty()) wrapper.in(CheckIn::getStudentId, studentIds);
        wrapper.orderByDesc(CheckIn::getCheckDate, CheckIn::getCheckTime);
        List<CheckIn> checkIns = checkInMapper.selectList(wrapper);

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("打卡记录");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"学号", "姓名", "楼栋", "宿舍号", "打卡日期", "打卡时间", "状态", "是否晚归"};
        for (int i = 0; i < headers.length; i++) headerRow.createCell(i).setCellValue(headers[i]);

        int rowNum = 1;
        for (CheckIn c : checkIns) {
            Row row = sheet.createRow(rowNum++);
            Student stu = studentMap.get(c.getStudentId());
            row.createCell(0).setCellValue(stu != null ? stu.getStudentNumber() : "");
            row.createCell(1).setCellValue(stu != null ? stu.getName() : "");
            Building b = stu != null && stu.getBuildingId() != null ? buildingMap.get(stu.getBuildingId()) : null;
            row.createCell(2).setCellValue(b != null ? b.getBuildingName() : "");
            Room r = stu != null && stu.getRoomId() != null ? roomMap.get(stu.getRoomId()) : null;
            row.createCell(3).setCellValue(r != null ? r.getRoomNumber() : "");
            row.createCell(4).setCellValue(c.getCheckDate() != null ? c.getCheckDate().toString() : "");
            row.createCell(5).setCellValue(c.getCheckTime() != null ? c.getCheckTime().toString() : "");
            row.createCell(6).setCellValue(c.getStatus() != null && c.getStatus() == 0 ? "补卡" : "正常");
            row.createCell(7).setCellValue(c.getIsLate() != null && c.getIsLate() == 1 ? "是" : "否");
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("打卡记录.xlsx", StandardCharsets.UTF_8));
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    /**
     * 删除打卡记录
     * 删除指定的打卡记录
     * @param id 打卡记录ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        checkInMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除打卡记录
     * 批量删除多条打卡记录
     * @param ids 打卡记录ID，多个用逗号分隔
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestParam String ids) {
        String[] idArr = ids.split(",");
        for (String id : idArr) {
            checkInMapper.deleteById(Long.parseLong(id));
        }
        return Result.success();
    }
}
