package com.dormitory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.common.Result;
import com.dormitory.entity.UtilityBill;
import com.dormitory.entity.Student;
import com.dormitory.entity.Room;
import com.dormitory.entity.Building;
import com.dormitory.entity.UtilityThreshold;
import com.dormitory.mapper.UtilityBillMapper;
import com.dormitory.mapper.StudentMapper;
import com.dormitory.mapper.RoomMapper;
import com.dormitory.mapper.BuildingMapper;
import com.dormitory.mapper.UtilityThresholdMapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.dormitory.common.Constant;
import com.dormitory.entity.UtilityWarning;
import com.dormitory.mapper.UtilityWarningMapper;
import com.dormitory.entity.Message;
import com.dormitory.mapper.MessageMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * UtilityController - 水电费管理控制器
 * 提供水电费账单、缴费、超限警告等功能的RESTful接口
 * @author 王和友
 * @since 2026
 */
@RestController
@RequestMapping("/api/utility")
public class UtilityController {

    private final UtilityBillMapper utilityBillMapper;
    private final StudentMapper studentMapper;
    private final RoomMapper roomMapper;
    private final BuildingMapper buildingMapper;
    private final UtilityThresholdMapper utilityThresholdMapper;
    private final UtilityWarningMapper utilityWarningMapper;
    private final MessageMapper messageMapper;

    public UtilityController(UtilityBillMapper utilityBillMapper, StudentMapper studentMapper,
                           RoomMapper roomMapper, BuildingMapper buildingMapper,
                           UtilityThresholdMapper utilityThresholdMapper,
                           UtilityWarningMapper utilityWarningMapper,
                           MessageMapper messageMapper) {
        this.utilityBillMapper = utilityBillMapper;
        this.studentMapper = studentMapper;
        this.roomMapper = roomMapper;
        this.buildingMapper = buildingMapper;
        this.utilityThresholdMapper = utilityThresholdMapper;
        this.utilityWarningMapper = utilityWarningMapper;
        this.messageMapper = messageMapper;
    }

    /**
     * 获取水电费账单列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param roomId 宿舍ID（可选）
     * @param buildingId 楼栋ID（可选）
     * @param year 年份（可选）
     * @param month 月份（可选）
     * @param isPaid 缴费状态（0-未缴费，1-已缴费）（可选）
     * @return 分页后的水电费账单列表
     */
    @GetMapping("/bill/list")
    public Result<Page<UtilityBill>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize,
                                          @RequestParam(required = false) Long roomId,
                                          @RequestParam(required = false) Long buildingId,
                                          @RequestParam(required = false) Integer year,
                                          @RequestParam(required = false) Integer month,
                                          @RequestParam(required = false) Integer isPaid) {
        Page<UtilityBill> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UtilityBill> wrapper = new LambdaQueryWrapper<>();
        
        if (roomId != null) {
            wrapper.eq(UtilityBill::getRoomId, roomId);
        }
        
        if (buildingId != null) {
            List<Room> rooms = roomMapper.selectList(new LambdaQueryWrapper<Room>().eq(Room::getBuildingId, buildingId));
            Set<Long> roomIds = rooms.stream().map(Room::getId).collect(Collectors.toSet());
            if (!roomIds.isEmpty()) {
                wrapper.in(UtilityBill::getRoomId, roomIds);
            } else {
                wrapper.eq(UtilityBill::getId, -1);
            }
        }
        
        if (year != null) {
            wrapper.eq(UtilityBill::getYear, year);
        }
        if (month != null) {
            wrapper.eq(UtilityBill::getMonth, month);
        }
        if (isPaid != null) {
            wrapper.eq(UtilityBill::getIsPaid, isPaid);
        }
        wrapper.orderByDesc(UtilityBill::getYear, UtilityBill::getMonth);
        Page<UtilityBill> result = utilityBillMapper.selectPage(page, wrapper);
        
        for (UtilityBill bill : result.getRecords()) {
            if (bill.getWaterUsage() != null && bill.getElectricUsage() != null) {
                Room room = roomMapper.selectById(bill.getRoomId());
                if (room != null) {
                    LambdaQueryWrapper<UtilityThreshold> tw = new LambdaQueryWrapper<>();
                    tw.eq(UtilityThreshold::getRoomType, room.getRoomType());
                    UtilityThreshold threshold = utilityThresholdMapper.selectOne(tw);
                    if (threshold != null) {
                        boolean isWaterOver = threshold.getWaterLimit() != null && 
                            bill.getWaterUsage().compareTo(threshold.getWaterLimit()) > 0;
                        boolean isElectricOver = threshold.getElectricLimit() != null && 
                            bill.getElectricUsage().compareTo(threshold.getElectricLimit()) > 0;
                        bill.setIsWaterOver(isWaterOver ? 1 : 0);
                        bill.setIsElectricOver(isElectricOver ? 1 : 0);
                    }
                }
            }
        }
        
        return Result.success(result);
    }

    /**
     * 根据ID获取水电费账单详情
     * @param id 账单ID
     * @return 水电费账单详情
     */
    @GetMapping("/bill/{id}")
    public Result<UtilityBill> getById(@PathVariable Long id) {
        UtilityBill bill = utilityBillMapper.selectById(id);
        return Result.success(bill);
    }

    /**
     * 新增水电费账单
     * 自动计算电费和水费（电费0.5元/度，水费2.0元/吨）
     * @param bill 水电费账单对象
     * @return 操作结果
     */
    @PostMapping("/bill")
    public Result<Void> addBill(@RequestBody UtilityBill bill) {
        if (bill.getElectricUsage() != null && bill.getElectricUsage().compareTo(java.math.BigDecimal.ZERO) >= 0) {
            bill.setElectricFee(bill.getElectricUsage().multiply(new java.math.BigDecimal("0.5")));
        }
        if (bill.getWaterUsage() != null && bill.getWaterUsage().compareTo(java.math.BigDecimal.ZERO) >= 0) {
            bill.setWaterFee(bill.getWaterUsage().multiply(new java.math.BigDecimal("2.0")));
        }
        if (bill.getElectricFee() == null) bill.setElectricFee(java.math.BigDecimal.ZERO);
        if (bill.getWaterFee() == null) bill.setWaterFee(java.math.BigDecimal.ZERO);
        bill.setTotalFee(bill.getElectricFee().add(bill.getWaterFee()));
        bill.setIsPaid(0);
        utilityBillMapper.insert(bill);
        
        Room room = roomMapper.selectById(bill.getRoomId());
        checkAndCreateWarning(bill, room);
        
        return Result.success();
    }

    /**
     * 更新水电费账单
     * 自动重新计算电费和水费
     * @param bill 水电费账单对象
     * @return 操作结果
     */
    @PutMapping("/bill")
    public Result<Void> updateBill(@RequestBody UtilityBill bill) {
        if (bill.getElectricUsage() != null && bill.getElectricUsage().compareTo(java.math.BigDecimal.ZERO) >= 0) {
            bill.setElectricFee(bill.getElectricUsage().multiply(new java.math.BigDecimal("0.5")));
        }
        if (bill.getWaterUsage() != null && bill.getWaterUsage().compareTo(java.math.BigDecimal.ZERO) >= 0) {
            bill.setWaterFee(bill.getWaterUsage().multiply(new java.math.BigDecimal("2.0")));
        }
        if (bill.getElectricFee() == null) bill.setElectricFee(java.math.BigDecimal.ZERO);
        if (bill.getWaterFee() == null) bill.setWaterFee(java.math.BigDecimal.ZERO);
        bill.setTotalFee(bill.getElectricFee().add(bill.getWaterFee()));
        utilityBillMapper.updateById(bill);
        
        Room room = roomMapper.selectById(bill.getRoomId());
        checkAndCreateWarning(bill, room);
        
        return Result.success();
    }

    /**
     * 删除水电费账单
     * 同时删除关联的超限警告记录
     * @param id 账单ID
     * @return 操作结果
     */
    @DeleteMapping("/bill/{id}")
    public Result<Void> deleteBill(@PathVariable Long id) {
        UtilityBill bill = utilityBillMapper.selectById(id);
        if (bill != null) {
            LambdaQueryWrapper<UtilityWarning> warningWrapper = new LambdaQueryWrapper<>();
            warningWrapper.eq(UtilityWarning::getRoomId, bill.getRoomId())
                .eq(UtilityWarning::getYear, bill.getYear())
                .eq(UtilityWarning::getMonth, bill.getMonth());
            utilityWarningMapper.delete(warningWrapper);
        }
        utilityBillMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 导入水电费账单（Excel文件）
     * 支持批量导入账单数据，自动计算费用
     * @param file Excel文件
     * @param buildingId 楼栋ID
     * @return 导入结果，包含成功数量、失败数量和错误信息
     */
    @PostMapping("/bill/import")
    public Result<Map<String, Object>> importBills(@RequestParam("file") MultipartFile file,
                                                     @RequestParam Long buildingId) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }
        
        try {
            List<Room> rooms = roomMapper.selectList(
                new LambdaQueryWrapper<Room>().eq(Room::getBuildingId, buildingId)
            );
            Map<String, Room> roomNumberMap = new HashMap<>();
            for (Room r : rooms) {
                roomNumberMap.put(r.getRoomNumber(), r);
            }
            
            List<UtilityThreshold> thresholds = new ArrayList<>();
            try {
                thresholds = (List<UtilityThreshold>) utilityThresholdMapper.selectList(null);
            } catch (Exception e) {
            }
            
            Map<Integer, UtilityThreshold> thresholdMap = new HashMap<>();
            for (UtilityThreshold t : thresholds) {
                thresholdMap.put(t.getRoomType(), t);
            }
            
            List<Building> buildings = buildingMapper.selectList(null);
            Map<Long, String> buildingNameMap = new HashMap<>();
            for (Building b : buildings) {
                buildingNameMap.put(b.getId(), b.getBuildingName());
            }
            
            List<UtilityBill> billsToInsert = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            List<String> successRooms = new ArrayList<>();
            
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    String roomNumber = getCellValue(row.getCell(0));
                    String yearStr = getCellValue(row.getCell(1));
                    String monthStr = getCellValue(row.getCell(2));
                    String waterUsageStr = getCellValue(row.getCell(3));
                    String electricUsageStr = getCellValue(row.getCell(4));
                    
                    if (roomNumber == null || roomNumber.isEmpty()) {
                        errors.add("第" + (i + 1) + "行：宿舍号为空");
                        continue;
                    }
                    
                    Room room = roomNumberMap.get(roomNumber);
                    if (room == null) {
                        errors.add("第" + (i + 1) + "行：宿舍号[" + roomNumber + "]不存在");
                        continue;
                    }
                    
                    if (!room.getBuildingId().equals(buildingId)) {
                        errors.add("第" + (i + 1) + "行：宿舍号[" + roomNumber + "]不属于本楼栋");
                        continue;
                    }
                    
                    Integer year = yearStr != null ? Integer.parseInt(yearStr.replace(".0", "")) : null;
                    Integer month = monthStr != null ? Integer.parseInt(monthStr.replace(".0", "")) : null;
                    
                    if (year == null || month == null) {
                        errors.add("第" + (i + 1) + "行：年份或月份为空");
                        continue;
                    }
                    
                    LambdaQueryWrapper<UtilityBill> checkWrapper = new LambdaQueryWrapper<>();
                    checkWrapper.eq(UtilityBill::getRoomId, room.getId());
                    checkWrapper.eq(UtilityBill::getYear, year);
                    checkWrapper.eq(UtilityBill::getMonth, month);
                    UtilityBill existing = utilityBillMapper.selectOne(checkWrapper);
                    if (existing != null) {
                        errors.add("第" + (i + 1) + "行：宿舍号[" + roomNumber + "] " + year + "年" + month + "月账单已存在");
                        continue;
                    }
                    
                    UtilityBill bill = new UtilityBill();
                    bill.setRoomId(room.getId());
                    bill.setYear(year);
                    bill.setMonth(month);
                    
                    java.math.BigDecimal waterUsage = waterUsageStr != null && !waterUsageStr.isEmpty() 
                        ? new java.math.BigDecimal(waterUsageStr) : java.math.BigDecimal.ZERO;
                    java.math.BigDecimal electricUsage = electricUsageStr != null && !electricUsageStr.isEmpty() 
                        ? new java.math.BigDecimal(electricUsageStr) : java.math.BigDecimal.ZERO;
                    
                    bill.setWaterUsage(waterUsage);
                    bill.setElectricUsage(electricUsage);
                    
                    java.math.BigDecimal electricPrice = new java.math.BigDecimal("0.5");
                    java.math.BigDecimal waterPrice = new java.math.BigDecimal("2.0");
                    
                    bill.setElectricFee(electricUsage.multiply(electricPrice));
                    bill.setWaterFee(waterUsage.multiply(waterPrice));
                    bill.setTotalFee(bill.getElectricFee().add(bill.getWaterFee()));
                    bill.setIsPaid(0);
                    
                    billsToInsert.add(bill);
                    successRooms.add(roomNumber);
                    
                } catch (Exception e) {
                    errors.add("第" + (i + 1) + "行：" + e.getMessage());
                }
            }
            
            workbook.close();
            
            int successCount = 0;
            for (UtilityBill bill : billsToInsert) {
                utilityBillMapper.insert(bill);
                Room room = roomMapper.selectById(bill.getRoomId());
                checkAndCreateWarning(bill, room);
                successCount++;
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("successCount", successCount);
            result.put("errorCount", errors.size());
            result.put("errors", errors);
            result.put("successRooms", successRooms);
            
            return Result.success(result);
            
        } catch (Exception e) {
            return Result.error("导入失败：" + e.getMessage());
        }
    }
    
    private String getCellValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: 
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val)) {
                    return String.valueOf((int) val);
                }
                return String.valueOf(val);
            default: return null;
        }
    }

    /**
     * 支付水电费账单
     * @param id 账单ID
     * @param payMethod 支付方式
     * @return 操作结果
     */
    @PostMapping("/bill/pay/{id}")
    public Result<Void> pay(@PathVariable Long id, @RequestParam String payMethod) {
        UtilityBill bill = utilityBillMapper.selectById(id);
        if (bill.getIsPaid() == 1) {
            return Result.error("该账单已支付");
        }
        bill.setIsPaid(1);
        bill.setPayTime(LocalDateTime.now());
        bill.setPayMethod(payMethod);
        utilityBillMapper.updateById(bill);
        return Result.success();
    }

    /**
     * 根据学生ID获取水电费账单
     * 获取该学生所在宿舍的所有账单记录
     * @param studentId 学生ID
     * @return 水电费账单列表
     */
    @GetMapping("/bill/student/{studentId}")
    public Result<List<UtilityBill>> getByStudentId(@PathVariable Long studentId) {
        Student student = studentMapper.selectById(studentId);
        if (student == null || student.getRoomId() == null) {
            return Result.success(List.of());
        }
        LambdaQueryWrapper<UtilityBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UtilityBill::getRoomId, student.getRoomId());
        wrapper.orderByDesc(UtilityBill::getYear, UtilityBill::getMonth);
        List<UtilityBill> bills = utilityBillMapper.selectList(wrapper);
        return Result.success(bills);
    }

    /**
     * 根据宿舍ID获取水电费账单
     * @param roomId 宿舍ID
     * @return 水电费账单列表
     */
    @GetMapping("/bill/room/{roomId}")
    public Result<List<UtilityBill>> getByRoomId(@PathVariable Long roomId) {
        LambdaQueryWrapper<UtilityBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UtilityBill::getRoomId, roomId);
        wrapper.orderByDesc(UtilityBill::getYear, UtilityBill::getMonth);
        List<UtilityBill> bills = utilityBillMapper.selectList(wrapper);
        return Result.success(bills);
    }

    /**
     * 获取楼栋水电费统计信息
     * 统计已缴费/未缴费数量、总用水量、总用电量、总费用等
     * @param buildingId 楼栋ID
     * @return 楼栋水电费统计信息
     */
    @GetMapping("/statistics/building/{buildingId}")
    public Result<Map<String, Object>> getBuildingStatistics(@PathVariable Long buildingId) {
        List<Room> rooms = roomMapper.selectList(new LambdaQueryWrapper<Room>().eq(Room::getBuildingId, buildingId));
        Set<Long> roomIds = rooms.stream().map(Room::getId).collect(Collectors.toSet());
        
        Map<String, Object> result = new HashMap<>();
        
        if (roomIds.isEmpty()) {
            result.put("totalRooms", 0);
            result.put("paidCount", 0);
            result.put("unpaidCount", 0);
            result.put("totalWaterUsage", 0);
            result.put("totalElectricUsage", 0);
            result.put("totalFee", 0);
            result.put("unpaidList", List.of());
            return Result.success(result);
        }
        
        List<UtilityBill> allBills = utilityBillMapper.selectList(
            new LambdaQueryWrapper<UtilityBill>().in(UtilityBill::getRoomId, roomIds)
        );
        
        int paidCount = 0;
        int unpaidCount = 0;
        java.math.BigDecimal totalWaterUsage = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalElectricUsage = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalFee = java.math.BigDecimal.ZERO;
        
        Map<Long, Room> roomMap = new HashMap<>();
        for (Room r : rooms) {
            roomMap.put(r.getId(), r);
        }
        
        List<Map<String, Object>> unpaidList = new ArrayList<>();
        
        for (UtilityBill bill : allBills) {
            if (bill.getIsPaid() == 1) {
                paidCount++;
            } else {
                unpaidCount++;
                Map<String, Object> item = new HashMap<>();
                item.put("id", bill.getId());
                item.put("year", bill.getYear());
                item.put("month", bill.getMonth());
                item.put("totalFee", bill.getTotalFee());
                Room room = roomMap.get(bill.getRoomId());
                if (room != null) {
                    item.put("roomNumber", room.getRoomNumber());
                }
                unpaidList.add(item);
            }
            
            if (bill.getWaterUsage() != null) {
                totalWaterUsage = totalWaterUsage.add(bill.getWaterUsage());
            }
            if (bill.getElectricUsage() != null) {
                totalElectricUsage = totalElectricUsage.add(bill.getElectricUsage());
            }
            if (bill.getTotalFee() != null) {
                totalFee = totalFee.add(bill.getTotalFee());
            }
        }
        
        result.put("totalRooms", rooms.size());
        result.put("paidCount", paidCount);
        result.put("unpaidCount", unpaidCount);
        result.put("totalWaterUsage", totalWaterUsage);
        result.put("totalElectricUsage", totalElectricUsage);
        result.put("totalFee", totalFee);
        result.put("unpaidList", unpaidList);
        
        return Result.success(result);
    }

    /**
     * 根据楼栋ID获取宿舍列表
     * @param buildingId 楼栋ID
     * @return 宿舍列表
     */
    @GetMapping("/room/list/building/{buildingId}")
    public Result<List<Room>> getRoomsByBuilding(@PathVariable Long buildingId) {
        List<Room> rooms = roomMapper.selectList(new LambdaQueryWrapper<Room>().eq(Room::getBuildingId, buildingId));
        return Result.success(rooms);
    }

    /**
     * 获取所有楼栋的水电费统计信息
     * 汇总各楼栋的缴费情况、用水用电量等数据
     * @return 所有楼栋的水电费统计信息
     */
    @GetMapping("/statistics/all")
    public Result<Map<String, Object>> getAllStatistics() {
        List<Building> buildings = buildingMapper.selectList(null);
        List<UtilityBill> allBills = utilityBillMapper.selectList(null);
        
        Map<String, Object> result = new HashMap<>();
        
        int totalPaid = 0;
        int totalUnpaid = 0;
        java.math.BigDecimal totalWaterUsage = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalElectricUsage = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalFee = java.math.BigDecimal.ZERO;
        
        Map<Long, Building> buildingMap = new HashMap<>();
        for (Building b : buildings) {
            buildingMap.put(b.getId(), b);
        }
        
        List<Map<String, Object>> buildingStats = new ArrayList<>();
        
        for (Building building : buildings) {
            List<Room> rooms = roomMapper.selectList(new LambdaQueryWrapper<Room>().eq(Room::getBuildingId, building.getId()));
            Set<Long> roomIds = rooms.stream().map(Room::getId).collect(Collectors.toSet());
            
            int paid = 0;
            int unpaid = 0;
            java.math.BigDecimal waterUsage = java.math.BigDecimal.ZERO;
            java.math.BigDecimal electricUsage = java.math.BigDecimal.ZERO;
            
            for (UtilityBill bill : allBills) {
                if (roomIds.contains(bill.getRoomId())) {
                    if (bill.getIsPaid() == 1) {
                        paid++;
                    } else {
                        unpaid++;
                    }
                    if (bill.getWaterUsage() != null) {
                        waterUsage = waterUsage.add(bill.getWaterUsage());
                    }
                    if (bill.getElectricUsage() != null) {
                        electricUsage = electricUsage.add(bill.getElectricUsage());
                    }
                }
            }
            
            Map<String, Object> bs = new HashMap<>();
            bs.put("buildingId", building.getId());
            bs.put("buildingName", building.getBuildingName());
            bs.put("paidCount", paid);
            bs.put("unpaidCount", unpaid);
            bs.put("waterUsage", waterUsage);
            bs.put("electricUsage", electricUsage);
            buildingStats.add(bs);
            
            totalPaid += paid;
            totalUnpaid += unpaid;
            totalWaterUsage = totalWaterUsage.add(waterUsage);
            totalElectricUsage = totalElectricUsage.add(electricUsage);
        }
        
        result.put("totalPaid", totalPaid);
        result.put("totalUnpaid", totalUnpaid);
        result.put("totalWaterUsage", totalWaterUsage);
        result.put("totalElectricUsage", totalElectricUsage);
        result.put("totalFee", totalFee);
        result.put("buildingStats", buildingStats);
        
        return Result.success(result);
    }

    /**
     * 获取水电费超限警告列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param buildingId 楼栋ID（可选）
     * @param year 年份（可选）
     * @param month 月份（可选）
     * @param status 处理状态（0-未处理，1-已处理）（可选）
     * @return 分页后的警告列表
     */
    @GetMapping("/warning/list")
    public Result<Page<Map<String, Object>>> warningList(@RequestParam(defaultValue = "1") Integer pageNum,
                                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                                        @RequestParam(required = false) Long buildingId,
                                                        @RequestParam(required = false) Integer year,
                                                        @RequestParam(required = false) Integer month,
                                                        @RequestParam(required = false) Integer status) {
        Page<UtilityWarning> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UtilityWarning> wrapper = new LambdaQueryWrapper<>();
        
        if (buildingId != null) {
            wrapper.eq(UtilityWarning::getBuildingId, buildingId);
        }
        if (year != null) {
            wrapper.eq(UtilityWarning::getYear, year);
        }
        if (month != null) {
            wrapper.eq(UtilityWarning::getMonth, month);
        }
        if (status != null) {
            wrapper.eq(UtilityWarning::getStatus, status);
        }
        
        wrapper.orderByDesc(UtilityWarning::getCreateTime);
        Page<UtilityWarning> result = utilityWarningMapper.selectPage(page, wrapper);
        
        Map<Long, String> buildingNameMap = new HashMap<>();
        Map<Long, String> roomNumberMap = new HashMap<>();
        
        Set<Long> buildingIds = result.getRecords().stream()
            .map(UtilityWarning::getBuildingId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> roomIds = result.getRecords().stream()
            .map(UtilityWarning::getRoomId).filter(Objects::nonNull).collect(Collectors.toSet());
        
        if (!buildingIds.isEmpty()) {
            List<Building> buildings = buildingMapper.selectBatchIds(buildingIds);
            for (Building b : buildings) {
                buildingNameMap.put(b.getId(), b.getBuildingName());
            }
        }
        
        if (!roomIds.isEmpty()) {
            List<Room> rooms = roomMapper.selectBatchIds(roomIds);
            for (Room r : rooms) {
                roomNumberMap.put(r.getId(), r.getRoomNumber());
            }
        }
        
        List<Map<String, Object>> records = new ArrayList<>();
        for (UtilityWarning w : result.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", w.getId());
            map.put("roomId", w.getRoomId());
            map.put("roomNumber", roomNumberMap.get(w.getRoomId()));
            map.put("buildingId", w.getBuildingId());
            map.put("buildingName", buildingNameMap.get(w.getBuildingId()));
            map.put("year", w.getYear());
            map.put("month", w.getMonth());
            map.put("waterUsage", w.getWaterUsage());
            map.put("waterLimit", w.getWaterLimit());
            map.put("electricUsage", w.getElectricUsage());
            map.put("electricLimit", w.getElectricLimit());
            map.put("isWaterOver", w.getIsWaterOver());
            map.put("isElectricOver", w.getIsElectricOver());
            map.put("status", w.getStatus());
            map.put("createTime", w.getCreateTime());
            records.add(map);
        }
        
        Page<Map<String, Object>> resultPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        resultPage.setRecords(records);
        return Result.success(resultPage);
    }
    
    /**
     * 获取有警告记录的年份列表
     * @return 年份列表
     */
    @GetMapping("/warning/years")
    public Result<List<Integer>> getWarningYears() {
        LambdaQueryWrapper<UtilityWarning> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(UtilityWarning::getYear);
        wrapper.isNotNull(UtilityWarning::getYear);
        wrapper.groupBy(UtilityWarning::getYear);
        wrapper.orderByDesc(UtilityWarning::getYear);
        List<UtilityWarning> list = utilityWarningMapper.selectList(wrapper);
        List<Integer> years = list.stream()
            .map(UtilityWarning::getYear)
            .filter(Objects::nonNull)
            .distinct()
            .sorted((a, b) -> b - a)
            .collect(Collectors.toList());
        if (years.isEmpty()) {
            years = Arrays.asList(2024, 2025, 2026);
        }
        return Result.success(years);
    }

    /**
     * 处理水电费超限警告
     * 将警告状态标记为已处理
     * @param id 警告ID
     * @return 操作结果
     */
    @PutMapping("/warning/{id}/process")
    public Result<Void> processWarning(@PathVariable Long id) {
        UtilityWarning warning = utilityWarningMapper.selectById(id);
        if (warning != null) {
            warning.setStatus(1);
            utilityWarningMapper.updateById(warning);
        }
        return Result.success();
    }
    
    /**
     * 删除水电费超限警告
     * @param id 警告ID
     * @return 操作结果
     */
    @DeleteMapping("/warning/{id}")
    public Result<Void> deleteWarning(@PathVariable Long id) {
        utilityWarningMapper.deleteById(id);
        return Result.success();
    }
    
    /**
     * 重新检查所有水电费账单并生成警告
     * 遍历所有账单，检查是否超限，更新警告记录
     * @return 检查结果，包含检查数量、新增警告数量、删除警告数量
     */
    @PostMapping("/warning/recheck")
    public Result<Map<String, Object>> recheckWarnings() {
        Map<String, Object> result = new HashMap<>();
        int checked = 0;
        int warningsCreated = 0;
        int warningsRemoved = 0;
        
        List<UtilityBill> bills = utilityBillMapper.selectList(null);
        Map<Long, Room> roomMap = new HashMap<>();
        List<Room> rooms = roomMapper.selectList(null);
        for (Room r : rooms) {
            roomMap.put(r.getId(), r);
        }
        
        Map<Integer, UtilityThreshold> thresholdMap = new HashMap<>();
        List<UtilityThreshold> thresholds = utilityThresholdMapper.selectList(null);
        for (UtilityThreshold t : thresholds) {
            thresholdMap.put(t.getRoomType(), t);
        }
        
        for (UtilityBill bill : bills) {
            checked++;
            Room room = roomMap.get(bill.getRoomId());
            if (room == null) continue;
            
            UtilityThreshold threshold = thresholdMap.get(room.getRoomType());
            if (threshold == null) continue;
            
            boolean isWaterOver = threshold.getWaterLimit() != null && 
                bill.getWaterUsage() != null && 
                bill.getWaterUsage().compareTo(threshold.getWaterLimit()) > 0;
            
            boolean isElectricOver = threshold.getElectricLimit() != null && 
                bill.getElectricUsage() != null && 
                bill.getElectricUsage().compareTo(threshold.getElectricLimit()) > 0;
            
            LambdaQueryWrapper<UtilityWarning> existingWrapper = new LambdaQueryWrapper<>();
            existingWrapper.eq(UtilityWarning::getRoomId, bill.getRoomId())
                .eq(UtilityWarning::getYear, bill.getYear())
                .eq(UtilityWarning::getMonth, bill.getMonth());
            UtilityWarning existingWarning = utilityWarningMapper.selectOne(existingWrapper);
            
            if (isWaterOver || isElectricOver) {
                if (existingWarning == null) {
                    UtilityWarning warning = new UtilityWarning();
                    warning.setRoomId(bill.getRoomId());
                    warning.setBuildingId(room.getBuildingId());
                    warning.setYear(bill.getYear());
                    warning.setMonth(bill.getMonth());
                    warning.setWaterUsage(bill.getWaterUsage());
                    warning.setWaterLimit(threshold.getWaterLimit());
                    warning.setElectricUsage(bill.getElectricUsage());
                    warning.setElectricLimit(threshold.getElectricLimit());
                    warning.setIsWaterOver(isWaterOver ? 1 : 0);
                    warning.setIsElectricOver(isElectricOver ? 1 : 0);
                    warning.setStatus(0);
                    warning.setCreateTime(LocalDateTime.now());
                    utilityWarningMapper.insert(warning);
                    warningsCreated++;
                } else {
                    existingWarning.setWaterUsage(bill.getWaterUsage());
                    existingWarning.setWaterLimit(threshold.getWaterLimit());
                    existingWarning.setElectricUsage(bill.getElectricUsage());
                    existingWarning.setElectricLimit(threshold.getElectricLimit());
                    existingWarning.setIsWaterOver(isWaterOver ? 1 : 0);
                    existingWarning.setIsElectricOver(isElectricOver ? 1 : 0);
                    utilityWarningMapper.updateById(existingWarning);
                }
            } else {
                if (existingWarning != null) {
                    utilityWarningMapper.deleteById(existingWarning.getId());
                    warningsRemoved++;
                }
            }
        }
        
        result.put("checked", checked);
        result.put("warningsCreated", warningsCreated);
        result.put("warningsRemoved", warningsRemoved);
        return Result.success(result);
    }

    private void checkAndCreateWarning(UtilityBill bill, Room room) {
        try {
            if (room == null || room.getBuildingId() == null) {
                return;
            }
            
            LambdaQueryWrapper<UtilityThreshold> thresholdWrapper = new LambdaQueryWrapper<>();
            thresholdWrapper.eq(UtilityThreshold::getRoomType, room.getRoomType());
            UtilityThreshold threshold = utilityThresholdMapper.selectOne(thresholdWrapper);
            
            if (threshold == null) {
                return;
            }
            
            boolean isWaterOver = threshold.getWaterLimit() != null && 
                bill.getWaterUsage() != null && 
                bill.getWaterUsage().compareTo(threshold.getWaterLimit()) > 0;
            
            boolean isElectricOver = threshold.getElectricLimit() != null && 
                bill.getElectricUsage() != null && 
                bill.getElectricUsage().compareTo(threshold.getElectricLimit()) > 0;
            
            if (isWaterOver || isElectricOver) {
                UtilityWarning warning = new UtilityWarning();
                warning.setRoomId(bill.getRoomId());
                warning.setBuildingId(room.getBuildingId());
                warning.setYear(bill.getYear());
                warning.setMonth(bill.getMonth());
                warning.setWaterUsage(bill.getWaterUsage());
                warning.setWaterLimit(threshold.getWaterLimit());
                warning.setElectricUsage(bill.getElectricUsage());
                warning.setElectricLimit(threshold.getElectricLimit());
                warning.setIsWaterOver(isWaterOver ? 1 : 0);
                warning.setIsElectricOver(isElectricOver ? 1 : 0);
                warning.setStatus(0);
                warning.setCreateTime(LocalDateTime.now());
                utilityWarningMapper.insert(warning);
                
                StringBuilder messageContent = new StringBuilder("本宿舍");
                if (isWaterOver && isElectricOver) {
                    messageContent.append("用水用电均超限");
                } else if (isWaterOver) {
                    messageContent.append("用水超限");
                } else {
                    messageContent.append("用电超限");
                }
                messageContent.append("，请节约使用！");
                
                LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
                studentWrapper.eq(Student::getRoomId, bill.getRoomId());
                List<Student> students = studentMapper.selectList(studentWrapper);
                
                for (Student student : students) {
                    Message message = new Message();
                    message.setUserId(student.getId());
                    message.setUserType(2);
                    message.setTitle("水电费超限提醒");
                    message.setContent(messageContent.toString());
                    message.setIsRead(0);
                    message.setCreateTime(LocalDateTime.now());
                    messageMapper.insert(message);
                }
            }
        } catch (Exception e) {
        }
    }
}
