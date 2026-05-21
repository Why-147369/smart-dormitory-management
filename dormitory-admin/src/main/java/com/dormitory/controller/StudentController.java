package com.dormitory.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.common.Constant;
import com.dormitory.common.Result;
import com.dormitory.entity.*;
import com.dormitory.mapper.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * StudentController - 学生管理控制器
 * 处理学生的增删改查、导入导出、档案信息管理等请求
 * @author 王和友
 * @since 2026
 */
@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentMapper studentMapper;
    private final RoomMapper roomMapper;
    private final BuildingMapper buildingMapper;
    private final BedMapper bedMapper;
    private final PasswordEncoder passwordEncoder;
    private final DormitoryManagerMapper dormitoryManagerMapper;

    public StudentController(StudentMapper studentMapper, RoomMapper roomMapper,
                            BuildingMapper buildingMapper, BedMapper bedMapper,
                            PasswordEncoder passwordEncoder,
                            DormitoryManagerMapper dormitoryManagerMapper) {
        this.studentMapper = studentMapper;
        this.roomMapper = roomMapper;
        this.buildingMapper = buildingMapper;
        this.bedMapper = bedMapper;
        this.passwordEncoder = passwordEncoder;
        this.dormitoryManagerMapper = dormitoryManagerMapper;
    }

    /**
     * 根据ID获取学生信息
     * 返回学生的基本信息和住宿信息
     * @param id 学生ID
     * @return 学生信息，包含基本信息和宿舍信息
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            return Result.error("学生不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", student.getId());
        result.put("studentNumber", student.getStudentNumber());
        result.put("name", student.getName());
        result.put("gender", student.getGender());
        result.put("college", student.getCollege());
        result.put("major", student.getMajor());
        result.put("className", student.getClassName());
        result.put("phone", student.getPhone());
        result.put("avatar", student.getAvatar());
        result.put("buildingId", student.getBuildingId());
        result.put("roomId", student.getRoomId());
        result.put("bedNumber", student.getBedNumber());
        
        if (student.getRoomId() != null) {
            Room room = roomMapper.selectById(student.getRoomId());
            if (room != null) {
                result.put("roomNumber", room.getRoomNumber());
                if (room.getBuildingId() != null) {
                    Building building = buildingMapper.selectById(room.getBuildingId());
                    if (building != null) {
                        result.put("buildingName", building.getBuildingName());
                    }
                }
            }
        }
        
        return Result.success(result);
    }

    /**
     * 获取学生详细信息及室友列表
     * 返回学生信息、房间信息、楼栋信息和室友列表
     * @param id 学生ID
     * @return 学生详细信息，包含室友列表
     */
    @GetMapping("/info/{id}")
    public Result<Object> getInfo(@PathVariable Long id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            return Result.error("学生不存在");
        }

        Room room = null;
        Building building = null;
        List<Student> roommates = null;

        if (student.getRoomId() != null) {
            room = roomMapper.selectById(student.getRoomId());
            if (room != null) {
                building = buildingMapper.selectById(room.getBuildingId());

                LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Student::getRoomId, student.getRoomId());
                wrapper.ne(Student::getId, student.getId());
                roommates = studentMapper.selectList(wrapper);
            }
        }

        return Result.success(java.util.Map.of(
                "student", student,
                "room", room,
                "building", building,
                "roommates", roommates
        ));
    }

    /**
     * 更新学生信息
     * 根据传入的学生对象更新数据库记录
     * @param student 学生信息
     * @return 更新结果
     */
    @CacheEvict(value = "studentList", allEntries = true)
    @PutMapping("/update")
    public Result<Void> update(@RequestBody Student student) {
        studentMapper.updateById(student);
        return Result.success();
    }

    /**
     * 获取学生列表
     * 分页查询学生信息，支持按楼栋和关键字筛选
     * @param pageNum 页码，默认1
     * @param pageSize 每页数量，默认10
     * @param buildingId 楼栋ID（可选）
     * @param keyword 搜索关键字（可选）
     * @return 分页学生列表
     *  分页查学生                                → 1 次
  2. 只查这 10 个学生的床位（WHERE IN 条件）      → 1 次
  3. 批量查这些床位对应的房间（selectBatchIds）    → 1 次
  4. 批量查这些房间对应的楼栋（selectBatchIds）    → 1 次
     */
    @Cacheable(value = "studentList", key = "#pageNum + '_' + #pageSize")
    @GetMapping("/list")
    public Result<IPage<Map<String, Object>>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                      @RequestParam(required = false) Long buildingId,
                                      @RequestParam(required = false) String keyword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdStr = authentication.getPrincipal().toString();
        Long userId = Long.parseLong(userIdStr);
        Integer userType = (Integer) authentication.getDetails();
        
        Long managerBuildingId = buildingId;
        if (userType == 2 && managerBuildingId == null) {
            DormitoryManager manager = dormitoryManagerMapper.selectById(userId);
            if (manager != null) {
                managerBuildingId = manager.getBuildingId();
            }
        }
        
        Page<Student> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        if (managerBuildingId != null) {
            wrapper.eq(Student::getBuildingId, managerBuildingId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Student::getName, keyword)
                    .or().like(Student::getStudentNumber, keyword)
                    .or().like(Student::getClassName, keyword)
                    .or().like(Student::getCollege, keyword)
                    .or().like(Student::getMajor, keyword));
        }
        Page<Student> result = studentMapper.selectPage(page, wrapper);
        
        List<Long> studentIds = result.getRecords().stream()
                .map(Student::getId)
                .collect(Collectors.toList());

        List<Bed> relatedBeds = new ArrayList<>();
        if (!studentIds.isEmpty()) {
            LambdaQueryWrapper<Bed> bedWrapper = new LambdaQueryWrapper<>();
            bedWrapper.in(Bed::getStudentId, studentIds);
            relatedBeds = bedMapper.selectList(bedWrapper);
        }

        Set<Long> roomIds = relatedBeds.stream()
                .map(Bed::getRoomId)
                .filter(r -> r != null)
                .collect(Collectors.toSet());

        Map<Long, Room> roomMap = new HashMap<>();
        if (!roomIds.isEmpty()) {
            List<Room> rooms = roomMapper.selectBatchIds(roomIds);
            for (Room room : rooms) {
                roomMap.put(room.getId(), room);
            }
        }

        Set<Long> buildingIds = roomMap.values().stream()
                .map(Room::getBuildingId)
                .filter(b -> b != null)
                .collect(Collectors.toSet());

        Map<Long, Building> buildingMap = new HashMap<>();
        if (!buildingIds.isEmpty()) {
            List<Building> buildings = buildingMapper.selectBatchIds(buildingIds);
            for (Building building : buildings) {
                buildingMap.put(building.getId(), building);
            }
        }

        Map<Long, Map<String, Object>> studentBedInfo = new HashMap<>();
        Map<Long, Long> studentBedIdMap = new HashMap<>();
        for (Bed bed : relatedBeds) {
            if (bed.getStudentId() != null) {
                studentBedIdMap.put(bed.getStudentId(), bed.getId());
                Room room = roomMap.get(bed.getRoomId());
                if (room != null) {
                    Map<String, Object> info = new HashMap<>();
                    info.put("roomId", room.getId());
                    info.put("roomNumber", room.getRoomNumber());
                    info.put("bedNumber", bed.getBedNumber());
                    Building building = buildingMap.get(room.getBuildingId());
                    if (building != null) {
                        info.put("buildingName", building.getBuildingName());
                    }
                    studentBedInfo.put(bed.getStudentId(), info);
                }
            }
        }

        Set<Long> studentBuildingIds = result.getRecords().stream()
                .map(Student::getBuildingId)
                .filter(b -> b != null)
                .collect(Collectors.toSet());
        Map<Long, Building> studentBuildingMap = new HashMap<>();
        if (!studentBuildingIds.isEmpty()) {
            List<Building> studentBuildings = buildingMapper.selectBatchIds(studentBuildingIds);
            for (Building b : studentBuildings) {
                studentBuildingMap.put(b.getId(), b);
            }
        }
        
        List<Map<String, Object>> records = new ArrayList<>();
        for (Student s : result.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("name", s.getName());
            map.put("studentNumber", s.getStudentNumber());
            map.put("gender", s.getGender());
            map.put("phone", s.getPhone());
            map.put("college", s.getCollege());
            map.put("major", s.getMajor());
            map.put("className", s.getClassName());
            map.put("status", s.getStatus());
            map.put("createTime", s.getCreateTime());
            
            if (s.getBuildingId() != null) {
                Building building = studentBuildingMap.get(s.getBuildingId());
                if (building != null) {
                    map.put("buildingName", building.getBuildingName());
                }
            }
            
            Map<String, Object> bedInfo = studentBedInfo.get(s.getId());
            if (bedInfo != null) {
                map.put("roomNumber", bedInfo.get("roomNumber"));
                map.put("bedNumber", bedInfo.get("bedNumber"));
                map.put("bedId", studentBedIdMap.get(s.getId()));
            } else {
                map.put("roomNumber", null);
                map.put("bedNumber", null);
                map.put("bedId", null);
            }
            
            records.add(map);
        }
        
        IPage<Map<String, Object>> resultPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        resultPage.setRecords(records);
        return Result.success(resultPage);
    }

    /**
     * 添加学生
     * 创建新的学生记录，默认密码为123456
     * @param student 学生信息
     * @return 添加结果
     */
    @CacheEvict(value = "studentList", allEntries = true)
    @PostMapping
    public Result<Void> add(@RequestBody Student student) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdStr = authentication.getPrincipal().toString();
        Long userId = Long.parseLong(userIdStr);
        Integer userType = (Integer) authentication.getDetails();
        
        if (userType == 2) {
            DormitoryManager manager = dormitoryManagerMapper.selectById(userId);
            if (manager != null && manager.getBuildingId() != null) {
                student.setBuildingId(manager.getBuildingId());
            }
        }
        
        student.setStatus(Constant.STATUS_NORMAL);
        if (student.getPassword() != null && !student.getPassword().isEmpty()) {
            student.setPassword(passwordEncoder.encode(student.getPassword()));
        } else {
            student.setPassword(passwordEncoder.encode("123456"));
        }
        studentMapper.insert(student);
        return Result.success();
    }

    /**
     * 删除学生
     * 根据ID删除学生记录
     * @param id 学生ID
     * @return 删除结果
     */
    @CacheEvict(value = "studentList", allEntries = true)
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        studentMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除学生
     * 根据ID列表批量删除学生记录
     * @param ids 学生ID列表
     * @return 删除结果
     */
    @CacheEvict(value = "studentList", allEntries = true)
    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        studentMapper.deleteBatchIds(ids);
        return Result.success();
    }

    /**
     * 根据楼栋ID获取学生列表
     * 查询指定楼栋下的所有学生
     * @param buildingId 楼栋ID
     * @return 学生列表
     */
    @GetMapping("/building/{buildingId}")
    public Result<List<Student>> getByBuildingId(@PathVariable Long buildingId) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getBuildingId, buildingId);
        List<Student> students = studentMapper.selectList(wrapper);
        students.forEach(s -> s.setPassword(null));
        return Result.success(students);
    }

    /**
     * 导入学生信息
     * 从Excel文件批量导入学生记录，默认密码为123456
     * @param file Excel文件
     * @return 导入结果
     */
    @CacheEvict(value = "studentList", allEntries = true)
    @PostMapping("/import")
    public Result<Void> importStudents(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }
        
        try {
            InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            
            List<Student> students = new ArrayList<>();
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                Cell cell0 = row.getCell(0);
                Cell cell1 = row.getCell(1);
                Cell cell2 = row.getCell(2);
                Cell cell3 = row.getCell(3);
                Cell cell4 = row.getCell(4);
                Cell cell5 = row.getCell(5);
                Cell cell6 = row.getCell(6);
                
                if (cell0 == null || getCellValue(cell0).isEmpty()) continue;
                
                Student student = new Student();
                student.setStudentNumber(getCellValue(cell0));
                student.setName(getCellValue(cell1));
                
                String genderStr = getCellValue(cell2);
                if ("男".equals(genderStr)) {
                    student.setGender(1);
                } else if ("女".equals(genderStr)) {
                    student.setGender(0);
                }
                
                student.setPhone(getCellValue(cell3));
                student.setCollege(getCellValue(cell4));
                student.setMajor(getCellValue(cell5));
                student.setClassName(getCellValue(cell6));
                student.setStatus(Constant.STATUS_NORMAL);
                student.setPassword(passwordEncoder.encode("123456"));
                
                students.add(student);
            }
            
            workbook.close();
            inputStream.close();
            
            if (!students.isEmpty()) {
                for (Student student : students) {
                    studentMapper.insert(student);
                }
            }
            
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("导入失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取Excel单元格的值
     * 支持字符串、数值和布尔类型
     * @param cell 单元格对象
     * @return 单元格值字符串
     */
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
    
    /**
     * 导出学生信息
     * 将学生信息导出为Excel文件下载
     * @param response HTTP响应对象
     */
    @GetMapping("/export")
    public void exportStudents(HttpServletResponse response) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userIdStr = authentication.getPrincipal().toString();
            Long userId = Long.parseLong(userIdStr);
            Integer userType = (Integer) authentication.getDetails();
            
            LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
            if (userType == 2) {
                DormitoryManager manager = dormitoryManagerMapper.selectById(userId);
                if (manager != null && manager.getBuildingId() != null) {
                    wrapper.eq(Student::getBuildingId, manager.getBuildingId());
                }
            }
            
            List<Student> students = studentMapper.selectList(wrapper);

            // 批量查询：只查当前学生关联的床位 → 房间 → 楼栋（避免 N+1）
            Map<Long, Map<String, Object>> studentBedInfo = new HashMap<>();
            if (!students.isEmpty()) {
                List<Long> studentIds = students.stream()
                        .map(Student::getId)
                        .collect(Collectors.toList());

                List<Bed> relatedBeds = bedMapper.selectList(
                        new LambdaQueryWrapper<Bed>().in(Bed::getStudentId, studentIds)
                );

                if (!relatedBeds.isEmpty()) {
                    Set<Long> roomIds = relatedBeds.stream()
                            .map(Bed::getRoomId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    Map<Long, Room> roomMap = new HashMap<>();
                    if (!roomIds.isEmpty()) {
                        for (Room r : roomMapper.selectBatchIds(roomIds)) {
                            roomMap.put(r.getId(), r);
                        }
                    }

                    Set<Long> buildingIds = roomMap.values().stream()
                            .map(Room::getBuildingId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    Map<Long, Building> buildingMap = new HashMap<>();
                    if (!buildingIds.isEmpty()) {
                        for (Building b : buildingMapper.selectBatchIds(buildingIds)) {
                            buildingMap.put(b.getId(), b);
                        }
                    }

                    for (Bed bed : relatedBeds) {
                        if (bed.getStudentId() != null) {
                            Room room = roomMap.get(bed.getRoomId());
                            if (room != null) {
                                Map<String, Object> info = new HashMap<>();
                                info.put("roomNumber", room.getRoomNumber());
                                info.put("bedNumber", bed.getBedNumber());
                                Building building = buildingMap.get(room.getBuildingId());
                                if (building != null) {
                                    info.put("buildingName", building.getBuildingName());
                                }
                                studentBedInfo.put(bed.getStudentId(), info);
                            }
                        }
                    }
                }
            }
            
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("学生信息");
            
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("学号");
            headerRow.createCell(1).setCellValue("姓名");
            headerRow.createCell(2).setCellValue("性别");
            headerRow.createCell(3).setCellValue("学院");
            headerRow.createCell(4).setCellValue("专业");
            headerRow.createCell(5).setCellValue("班级");
            headerRow.createCell(6).setCellValue("电话");
            headerRow.createCell(7).setCellValue("楼栋");
            headerRow.createCell(8).setCellValue("宿舍号");
            headerRow.createCell(9).setCellValue("床位号");
            headerRow.createCell(10).setCellValue("状态");
            
            int rowNum = 1;
            for (Student student : students) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(student.getStudentNumber());
                row.createCell(1).setCellValue(student.getName());
                row.createCell(2).setCellValue(student.getGender() == 1 ? "男" : "女");
                row.createCell(3).setCellValue(student.getCollege());
                row.createCell(4).setCellValue(student.getMajor());
                row.createCell(5).setCellValue(student.getClassName());
                row.createCell(6).setCellValue(student.getPhone());
                
                Map<String, Object> bedInfo = studentBedInfo.get(student.getId());
                if (bedInfo != null) {
                    row.createCell(7).setCellValue(bedInfo.get("buildingName") != null ? bedInfo.get("buildingName").toString() : "");
                    row.createCell(8).setCellValue(bedInfo.get("roomNumber") != null ? bedInfo.get("roomNumber").toString() : "");
                    row.createCell(9).setCellValue(bedInfo.get("bedNumber") != null ? bedInfo.get("bedNumber").toString() : "");
                } else {
                    row.createCell(7).setCellValue("");
                    row.createCell(8).setCellValue("");
                    row.createCell(9).setCellValue("");
                }
                
                row.createCell(10).setCellValue(student.getStatus() == 1 ? "在校" : "离校");
            }
            
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=students.xlsx");
            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前登录学生的信息
     * 返回当前登录学生的详细信息
     * @return 当前学生信息
     */
    @GetMapping("/me")
    public Result<Map<String, Object>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        Student student = studentMapper.selectById(userId);
        if (student == null) {
            return Result.error("学生不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", student.getId());
        result.put("studentNumber", student.getStudentNumber());
        result.put("name", student.getName());
        result.put("gender", student.getGender());
        result.put("college", student.getCollege());
        result.put("major", student.getMajor());
        result.put("className", student.getClassName());
        result.put("phone", student.getPhone());
        result.put("avatar", student.getAvatar());
        result.put("buildingId", student.getBuildingId());
        result.put("roomId", student.getRoomId());
        result.put("bedNumber", student.getBedNumber());
        
        if (student.getRoomId() != null) {
            Room room = roomMapper.selectById(student.getRoomId());
            if (room != null) {
                result.put("roomNumber", room.getRoomNumber());
                if (room.getBuildingId() != null) {
                    Building building = buildingMapper.selectById(room.getBuildingId());
                    if (building != null) {
                        result.put("buildingName", building.getBuildingName());
                    }
                }
            }
        }
        
        if (student.getBedNumber() != null) {
            LambdaQueryWrapper<Bed> bedWrapper = new LambdaQueryWrapper<>();
            bedWrapper.eq(Bed::getStudentId, student.getId());
            Bed bed = bedMapper.selectOne(bedWrapper);
            if (bed != null) {
                result.put("bedNumber", bed.getBedNumber());
            }
        }
        
        return Result.success(result);
    }

    /**
     * 更新当前学生个人资料
     * 允许学生修改自己的基本信息
     * @param params 包含姓名、电话、头像等信息的Map
     * @return 更新结果
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody Map<String, Object> params) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        Student student = studentMapper.selectById(userId);
        if (student == null) {
            return Result.error("学生不存在");
        }
        
        if (params.containsKey("name")) {
            student.setName((String) params.get("name"));
        }
        if (params.containsKey("phone")) {
            student.setPhone((String) params.get("phone"));
        }
        if (params.containsKey("avatar")) {
            student.setAvatar((String) params.get("avatar"));
        }
        
        studentMapper.updateById(student);
        return Result.success();
    }

    /**
     * 修改当前学生密码
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
        Student student = studentMapper.selectById(userId);
        if (student == null) {
            return Result.error("学生不存在");
        }
        
        if (!passwordEncoder.matches(oldPassword, student.getPassword())) {
            return Result.error("原密码错误");
        }
        
        student.setPassword(passwordEncoder.encode(newPassword));
        studentMapper.updateById(student);
        return Result.success();
    }
}
