package com.dormitory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.common.Result;
import com.dormitory.entity.Bed;
import com.dormitory.entity.Room;
import com.dormitory.entity.Building;
import com.dormitory.entity.Student;
import com.dormitory.mapper.BedMapper;
import com.dormitory.mapper.RoomMapper;
import com.dormitory.mapper.BuildingMapper;
import com.dormitory.mapper.StudentMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** BedController - 床位管理控制器，提供床位的增删改查、批量删除、学生分配、导入等功能
 * @author 王和友 @since 2026 */
@RestController
@RequestMapping("/api/bed")
public class BedController {

    private final BedMapper bedMapper;
    private final RoomMapper roomMapper;
    private final BuildingMapper buildingMapper;
    private final StudentMapper studentMapper;

    public BedController(BedMapper bedMapper, RoomMapper roomMapper, BuildingMapper buildingMapper, StudentMapper studentMapper) {
        this.bedMapper = bedMapper;
        this.roomMapper = roomMapper;
        this.buildingMapper = buildingMapper;
        this.studentMapper = studentMapper;
    }

    /** 获取床位列表，支持按宿舍ID和楼栋ID筛选，带分页
     * @param pageNum 页码，默认1
     * @param pageSize 每页条数，默认10
     * @param roomId 宿舍ID（可选）
     * @param buildingId 楼栋ID（可选）
     * @return 床位列表及总数 */
    @GetMapping("/list")
    public Result<Page<Map<String, Object>>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                                     @RequestParam(required = false) Long roomId,
                                                     @RequestParam(required = false) Long buildingId) {
        Page<Bed> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Bed> wrapper = new LambdaQueryWrapper<>();
        if (roomId != null) {
            wrapper.eq(Bed::getRoomId, roomId);
        }
        
        List<Long> targetRoomIds = null;
        if (buildingId != null) {
            LambdaQueryWrapper<Room> roomWrapper = new LambdaQueryWrapper<>();
            roomWrapper.eq(Room::getBuildingId, buildingId);
            List<Room> rooms = roomMapper.selectList(roomWrapper);
            targetRoomIds = rooms.stream().map(Room::getId).collect(Collectors.toList());
            if (targetRoomIds.isEmpty()) {
                targetRoomIds.add(-1L);
            }
            wrapper.in(Bed::getRoomId, targetRoomIds);
        }
        
        wrapper.orderByDesc(Bed::getId);
        Page<Bed> result = bedMapper.selectPage(page, wrapper);

        List<Long> roomIds = result.getRecords().stream().map(Bed::getRoomId).distinct().collect(Collectors.toList());
        Map<Long, Room> roomMap = new HashMap<>();
        if (!roomIds.isEmpty()) {
            List<Room> rooms = roomMapper.selectBatchIds(roomIds);
            for (Room room : rooms) {
                roomMap.put(room.getId(), room);
            }
        }

        List<Long> buildingIds = roomMap.values().stream().map(Room::getBuildingId).distinct().collect(Collectors.toList());
        Map<Long, Building> buildingMap = new HashMap<>();
        if (!buildingIds.isEmpty()) {
            List<Building> buildings = buildingMapper.selectBatchIds(buildingIds);
            for (Building building : buildings) {
                buildingMap.put(building.getId(), building);
            }
        }
        
        List<Long> studentIds = result.getRecords().stream()
            .map(Bed::getStudentId)
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, Student> studentMap = new HashMap<>();
        if (!studentIds.isEmpty()) {
            List<Student> students = studentMapper.selectBatchIds(studentIds);
            for (Student student : students) {
                studentMap.put(student.getId(), student);
            }
        }

        List<Map<String, Object>> records = new ArrayList<>();
        for (Bed bed : result.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", bed.getId());
            map.put("roomId", bed.getRoomId());
            map.put("bedNumber", bed.getBedNumber());
            map.put("status", bed.getStatus());
            map.put("studentId", bed.getStudentId());
            
            Room room = roomMap.get(bed.getRoomId());
            if (room != null) {
                map.put("roomNumber", room.getRoomNumber());
                Building building = buildingMap.get(room.getBuildingId());
                if (building != null) {
                    map.put("buildingName", building.getBuildingName());
                }
            }
            
            if (bed.getStudentId() != null) {
                Student student = studentMap.get(bed.getStudentId());
                if (student != null) {
                    map.put("studentName", student.getName());
                }
            }
            
            records.add(map);
        }

        Page<Map<String, Object>> resultPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        resultPage.setRecords(records);
        return Result.success(resultPage);
    }

    /** 根据ID获取床位详细信息
     * @param id 床位ID
     * @return 床位详细信息 */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        Bed bed = bedMapper.selectById(id);
        if (bed == null) {
            return Result.error("床位不存在");
        }
        
        Map<String, Object> map = new HashMap<>();
        map.put("id", bed.getId());
        map.put("roomId", bed.getRoomId());
        map.put("bedNumber", bed.getBedNumber());
        map.put("status", bed.getStatus());
        map.put("studentId", bed.getStudentId());
        
        Room room = roomMapper.selectById(bed.getRoomId());
        if (room != null) {
            map.put("roomNumber", room.getRoomNumber());
            Building building = buildingMapper.selectById(room.getBuildingId());
            if (building != null) {
                map.put("buildingName", building.getBuildingName());
            }
        }
        
        return Result.success(map);
    }

    /** 添加新床位
     * @param bed 床位信息
     * @return 操作结果 */
    @PostMapping
    public Result<Void> add(@RequestBody Bed bed) {
        bedMapper.insert(bed);
        return Result.success();
    }

    /** 更新床位信息
     * @param bed 床位信息
     * @return 操作结果 */
    @PutMapping
    public Result<Void> update(@RequestBody Bed bed) {
        bedMapper.updateById(bed);
        return Result.success();
    }

    /** 删除床位
     * @param id 床位ID
     * @return 操作结果 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bedMapper.deleteById(id);
        return Result.success();
    }
    
    /** 获取所有床位列表（无分页）
     * @param roomId 宿舍ID（可选）
     * @return 床位列表 */
    @GetMapping("/all")
    public Result<List<Map<String, Object>>> getAll(@RequestParam(required = false) Long roomId) {
        LambdaQueryWrapper<Bed> wrapper = new LambdaQueryWrapper<>();
        if (roomId != null) {
            wrapper.eq(Bed::getRoomId, roomId);
        }
        List<Bed> beds = bedMapper.selectList(wrapper);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Bed bed : beds) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", bed.getId());
            map.put("roomId", bed.getRoomId());
            map.put("bedNumber", bed.getBedNumber());
            map.put("status", bed.getStatus());
            map.put("studentId", bed.getStudentId());
            result.add(map);
        }
        return Result.success(result);
    }

    /** 分页获取床位列表，支持按楼栋ID、宿舍ID和状态筛选
     * @param pageNum 页码，默认1
     * @param pageSize 每页条数，默认10
     * @param buildingId 楼栋ID（可选）
     * @param roomId 宿舍ID（可选）
     * @param status 床位状态（可选）
     * @return 分页后的床位列表 */
    @GetMapping("/page")
    public Result<IPage<Map<String, Object>>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                                  @RequestParam(required = false) Long buildingId,
                                                  @RequestParam(required = false) Long roomId,
                                                  @RequestParam(required = false) Integer status) {
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        IPage<Map<String, Object>> result = bedMapper.selectBedPage(page, buildingId, roomId, status);
        return Result.success(result);
    }

    /** 批量删除床位
     * @param ids 床位ID列表
     * @return 操作结果 */
    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        bedMapper.deleteBatchIds(ids);
        return Result.success();
    }

    /** 导入床位信息（从Excel文件批量导入）
     * @param file Excel文件
     * @return 操作结果 */
    @PostMapping("/import")
    public Result<Void> importBeds(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }
        
        try {
            List<Room> allRooms = roomMapper.selectList(null);
            Map<String, Long> roomNumberMap = new HashMap<>();
            for (Room r : allRooms) {
                roomNumberMap.put(r.getRoomNumber(), r.getId());
            }
            
            InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            
            List<Bed> beds = new ArrayList<>();
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                Cell cell0 = row.getCell(0);
                Cell cell1 = row.getCell(1);
                Cell cell2 = row.getCell(2);
                Cell cell3 = row.getCell(3);
                
                if (cell0 == null || getCellValue(cell0).isEmpty()) continue;
                
                String roomNumber = getCellValue(cell1);
                Long roomId = roomNumberMap.get(roomNumber);
                
                if (roomId == null) {
                    continue;
                }
                
                Bed bed = new Bed();
                bed.setRoomId(roomId);
                bed.setBedNumber(getIntValue(cell2));
                bed.setStatus(getIntValue(cell3));
                
                beds.add(bed);
            }
            
            workbook.close();
            inputStream.close();
            
            if (!beds.isEmpty()) {
                for (Bed bed : beds) {
                    bedMapper.insert(bed);
                }
            }
            
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("导入失败: " + e.getMessage());
        }
    }
    
    /** 获取Excel单元格的值并转换为字符串
     * @param cell 单元格对象
     * @return 单元格字符串值 */
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
    
    /** 获取Excel单元格的值并转换为整数
     * @param cell 单元格对象
     * @return 单元格整数值 */
    private Integer getIntValue(Cell cell) {
        if (cell == null) return 0;
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return 0;
                }
            default:
                return 0;
        }
    }

    /** 获取床位分配页面列表，支持按楼栋、宿舍筛选和是否已分配学生筛选
     * @param pageNum 页码，默认1
     * @param pageSize 每页条数，默认10
     * @param buildingId 楼栋ID（可选）
     * @param roomId 宿舍ID（可选）
     * @param hasStudent 是否已分配学生（0-未分配，1-已分配，可选）
     * @return 分页后的床位分配列表 */
    @GetMapping("/assignmentPage")
    public Result<IPage<Map<String, Object>>> assignmentPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                                              @RequestParam(required = false) Long buildingId,
                                                              @RequestParam(required = false) Long roomId,
                                                              @RequestParam(required = false) Integer hasStudent) {
        List<Map<String, Object>> allBeds = bedMapper.selectAllBeds(buildingId, roomId, null);
        
        if (hasStudent != null) {
            allBeds = allBeds.stream()
                .filter(record -> {
                    Object studentId = record.get("studentId");
                    boolean hasStudentFlag = studentId != null;
                    return hasStudent == 0 ? !hasStudentFlag : hasStudentFlag;
                })
                .collect(Collectors.toList());
        }
        
        int total = allBeds.size();
        int start = (int) ((pageNum - 1) * pageSize);
        int end = Math.min(start + (int) pageSize, total);
        List<Map<String, Object>> pagedRecords = start < total && start < allBeds.size() 
            ? allBeds.subList(start, Math.min(end, allBeds.size())) 
            : new ArrayList<>();
        
        Page<Map<String, Object>> resultPage = new Page<>(pageNum, pageSize, total);
        resultPage.setRecords(pagedRecords);
        return Result.success(resultPage);
    }

    /** 获取未分配床位的学生列表
     * @return 未分配床位的学生列表 */
    @GetMapping("/unassignedStudents")
    public Result<List<Student>> getUnassignedStudents() {
        List<Student> allStudents = studentMapper.selectList(null);
        
        List<Bed> allBeds = bedMapper.selectList(null);
        List<Long> assignedStudentIds = allBeds.stream()
            .map(Bed::getStudentId)
            .filter(id -> id != null)
            .collect(Collectors.toList());
        
        List<Student> unassigned = allStudents.stream()
            .filter(s -> !assignedStudentIds.contains(s.getId()))
            .collect(Collectors.toList());
        
        return Result.success(unassigned);
    }

    /** 分配床位给学生
     * @param params 包含bedId和studentId的Map
     * @return 操作结果 */
    @PostMapping("/assign")
    public Result<Void> assignBed(@RequestBody Map<String, Long> params) {
        Long bedId = params.get("bedId");
        Long studentId = params.get("studentId");
        
        Bed bed = bedMapper.selectById(bedId);
        if (bed == null) {
            return Result.error("床位不存在");
        }
        
        if (bed.getStudentId() != null) {
            return Result.error("该床位已分配给学生");
        }
        
        bed.setStudentId(studentId);
        bed.setStatus(1);
        bedMapper.updateById(bed);
        
        Room room = roomMapper.selectById(bed.getRoomId());
        if (room != null) {
            room.setCurrentCount(room.getCurrentCount() + 1);
            roomMapper.updateById(room);
            
            Student student = studentMapper.selectById(studentId);
            if (student != null) {
                student.setBuildingId(room.getBuildingId());
                student.setRoomId(room.getId());
                student.setBedNumber(bed.getBedNumber());
                studentMapper.updateById(student);
            }
        }
        
        return Result.success();
    }

    /** 移除学生床位分配
     * @param params 包含bedId的Map
     * @return 操作结果 */
    @PostMapping("/remove")
    public Result<Void> removeBed(@RequestBody Map<String, Long> params) {
        Long bedId = params.get("bedId");
        
        Bed bed = bedMapper.selectById(bedId);
        if (bed == null) {
            return Result.error("床位不存在");
        }
        
        if (bed.getStudentId() == null) {
            return Result.error("该床位未分配学生");
        }
        
        Long studentId = bed.getStudentId();
        
        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Bed> updateWrapper = new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
        updateWrapper.eq("id", bedId);
        updateWrapper.set("student_id", (Long)null);
        updateWrapper.set("status", 0);
        bedMapper.update(null, updateWrapper);
        
        Room room = roomMapper.selectById(bed.getRoomId());
        if (room != null && room.getCurrentCount() > 0) {
            room.setCurrentCount(room.getCurrentCount() - 1);
            roomMapper.updateById(room);
        }
        
        if (studentId != null) {
            Student student = studentMapper.selectById(studentId);
            if (student != null) {
                student.setBuildingId(null);
                student.setRoomId(null);
                student.setBedNumber(null);
                studentMapper.updateById(student);
            }
        }
        
        return Result.success();
    }

    /** 导入学生床位分配信息（从Excel文件批量分配学生到床位）
     * @param file Excel文件，包含学号、楼栋名、宿舍号、床位号
     * @return 导入结果，包含成功和失败数量 */
    @PostMapping("/assign/import")
    public Result<String> importAssign(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }
        
        try {
            List<Student> allStudents = studentMapper.selectList(null);
            Map<String, Long> studentNumberMap = new HashMap<>();
            for (Student s : allStudents) {
                studentNumberMap.put(s.getStudentNumber(), s.getId());
            }
            
            List<Building> allBuildings = buildingMapper.selectList(null);
            Map<String, Long> buildingNameMap = new HashMap<>();
            for (Building b : allBuildings) {
                buildingNameMap.put(b.getBuildingName(), b.getId());
            }
            
            List<Room> allRooms = roomMapper.selectList(null);
            Map<Long, Map<String, Long>> buildingRoomMap = new HashMap<>();
            for (Room r : allRooms) {
                if (r.getBuildingId() != null) {
                    Map<String, Long> roomMap = buildingRoomMap.computeIfAbsent(r.getBuildingId(), k -> new HashMap<>());
                    roomMap.put(r.getRoomNumber(), r.getId());
                }
            }
            
            List<Bed> allBeds = bedMapper.selectList(null);
            Map<String, Bed> bedKeyMap = new HashMap<>();
            for (Bed b : allBeds) {
                Room r = roomMapper.selectById(b.getRoomId());
                if (r != null) {
                    String key = r.getId() + "_" + b.getBedNumber();
                    bedKeyMap.put(key, b);
                }
            }
            
            InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            
            int successCount = 0;
            int failCount = 0;
            StringBuilder errors = new StringBuilder();
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                Cell cell0 = row.getCell(0);
                Cell cell1 = row.getCell(1);
                Cell cell2 = row.getCell(2);
                Cell cell3 = row.getCell(3);
                
                if (cell0 == null || getCellValue(cell0).isEmpty()) continue;
                
                String studentNumber = getCellValue(cell0);
                String buildingName = getCellValue(cell1);
                String roomNumber = getCellValue(cell2);
                Integer bedNumber = getIntValue(cell3);
                
                Long studentId = studentNumberMap.get(studentNumber);
                if (studentId == null) {
                    failCount++;
                    errors.append("第").append(i + 1).append("行：学号").append(studentNumber).append("不存在；");
                    continue;
                }
                
                Long buildingId = buildingNameMap.get(buildingName);
                if (buildingId == null) {
                    failCount++;
                    errors.append("第").append(i + 1).append("行：楼栋").append(buildingName).append("不存在；");
                    continue;
                }
                
                Map<String, Long> roomMap = buildingRoomMap.get(buildingId);
                if (roomMap == null || !roomMap.containsKey(roomNumber)) {
                    failCount++;
                    errors.append("第").append(i + 1).append("行：宿舍").append(roomNumber).append("不存在；");
                    continue;
                }
                
                Long roomId = roomMap.get(roomNumber);
                String bedKey = roomId + "_" + bedNumber;
                Bed bed = bedKeyMap.get(bedKey);
                if (bed == null) {
                    failCount++;
                    errors.append("第").append(i + 1).append("行：床位").append(roomNumber).append("-").append(bedNumber).append("不存在；");
                    continue;
                }
                
                if (bed.getStudentId() != null) {
                    failCount++;
                    errors.append("第").append(i + 1).append("行：床位已被分配；");
                    continue;
                }
                
                bed.setStudentId(studentId);
                bed.setStatus(1);
                bedMapper.updateById(bed);
                
                Room room = roomMapper.selectById(bed.getRoomId());
                if (room != null) {
                    room.setCurrentCount(room.getCurrentCount() + 1);
                    roomMapper.updateById(room);
                    
                    Student student = studentMapper.selectById(studentId);
                    if (student != null) {
                        student.setBuildingId(room.getBuildingId());
                        student.setRoomId(room.getId());
                        student.setBedNumber(bed.getBedNumber());
                        studentMapper.updateById(student);
                    }
                }
                
                successCount++;
            }
            
            workbook.close();
            inputStream.close();
            
            if (failCount > 0) {
                return Result.success("导入完成，成功" + successCount + "条，失败" + failCount + "条：" + errors.toString());
            }
            return Result.success("导入成功，共分配" + successCount + "个床位");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("导入失败: " + e.getMessage());
        }
    }
}
