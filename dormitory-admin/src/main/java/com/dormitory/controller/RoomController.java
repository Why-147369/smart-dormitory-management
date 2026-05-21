package com.dormitory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dormitory.common.Constant;
import com.dormitory.common.Result;
import com.dormitory.entity.Building;
import com.dormitory.entity.Room;
import com.dormitory.mapper.BuildingMapper;
import com.dormitory.mapper.RoomMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** RoomController - 宿舍管理控制器，提供宿舍的增删改查、批量删除、导入等功能
 * @author 王和友 @since 2026 */
@RestController
@RequestMapping("/api/room")
public class RoomController {

    private final RoomMapper roomMapper;
    private final BuildingMapper buildingMapper;

    public RoomController(RoomMapper roomMapper, BuildingMapper buildingMapper) {
        this.roomMapper = roomMapper;
        this.buildingMapper = buildingMapper;
    }

    /** 获取宿舍列表，支持按楼栋ID筛选和分页
     * @param buildingId 楼栋ID（可选）
     * @param pageNum 页码，默认1
     * @param pageSize 每页条数，默认100
     * @return 宿舍列表及总数 */
    @Cacheable(value = "roomList")
    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam(required = false) Long buildingId,
                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "100") Integer pageSize) {
        Page<Room> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        if (buildingId != null) {
            wrapper.eq(Room::getBuildingId, buildingId);
        }
        Page<Room> roomPage = roomMapper.selectPage(page, wrapper);
        
        List<Long> buildingIds = roomPage.getRecords().stream()
            .map(Room::getBuildingId)
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());
        
        Map<Long, String> buildingMap = new HashMap<>();
        if (!buildingIds.isEmpty()) {
            List<Building> buildings = buildingMapper.selectBatchIds(buildingIds);
            for (Building b : buildings) {
                buildingMap.put(b.getId(), b.getBuildingName());
            }
        }
        
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Room r : roomPage.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("buildingId", r.getBuildingId());
            map.put("roomNumber", r.getRoomNumber());
            map.put("floor", r.getFloor());
            map.put("roomType", r.getRoomType());
            map.put("bedCount", r.getBedCount());
            map.put("currentCount", r.getCurrentCount());
            map.put("status", r.getStatus());
            map.put("createTime", r.getCreateTime());
            if (r.getBuildingId() != null) {
                map.put("buildingName", buildingMap.get(r.getBuildingId()));
            }
            result.add(map);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("records", result);
        response.put("total", roomPage.getTotal());
        return Result.success(response);
    }

    /** 分页获取宿舍列表，支持按楼栋ID和状态筛选
     * @param pageNum 页码，默认1
     * @param pageSize 每页条数，默认10
     * @param buildingId 楼栋ID（可选）
     * @param status 宿舍状态（可选）
     * @return 分页后的宿舍列表 */
    @GetMapping("/page")
    public Result<IPage<Map<String, Object>>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   @RequestParam(required = false) Long buildingId,
                                   @RequestParam(required = false) Integer status) {
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        
        IPage<Map<String, Object>> result = roomMapper.selectRoomPage(page, buildingId, status);
        return Result.success(result);
    }

    /** 根据ID获取宿舍信息
     * @param id 宿舍ID
     * @return 宿舍详细信息 */
    @GetMapping("/{id}")
    public Result<Room> getById(@PathVariable Long id) {
        Room room = roomMapper.selectById(id);
        return Result.success(room);
    }

    /** 添加新宿舍
     * @param room 宿舍信息
     * @return 操作结果 */
    @CacheEvict(value = "roomList", allEntries = true)
    @PostMapping
    public Result<Void> add(@RequestBody Room room) {
        if (room.getRoomType() == null) {
            room.setRoomType(4);
        }
        if (room.getBedCount() == null) {
            room.setBedCount(4);
        }
        if (room.getCurrentCount() == null) {
            room.setCurrentCount(0);
        }
        if (room.getStatus() == null) {
            room.setStatus(1);
        }
        roomMapper.insert(room);
        return Result.success();
    }

    /** 更新宿舍信息
     * @param room 宿舍信息
     * @return 操作结果 */
    @CacheEvict(value = "roomList", allEntries = true)
    @PutMapping
    public Result<Void> update(@RequestBody Room room) {
        roomMapper.updateById(room);
        return Result.success();
    }

    /** 删除宿舍
     * @param id 宿舍ID
     * @return 操作结果 */
    @CacheEvict(value = "roomList", allEntries = true)
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roomMapper.deleteById(id);
        return Result.success();
    }

    /** 批量删除宿舍
     * @param ids 宿舍ID列表
     * @return 操作结果 */
    @CacheEvict(value = "roomList", allEntries = true)
    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        roomMapper.deleteBatchIds(ids);
        return Result.success();
    }

    /** 根据楼栋ID获取宿舍列表
     * @param buildingId 楼栋ID
     * @return 宿舍列表 */
    @GetMapping("/building/{buildingId}")
    public Result<List<Room>> getByBuildingId(@PathVariable Long buildingId) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Room::getBuildingId, buildingId);
        List<Room> rooms = roomMapper.selectList(wrapper);
        return Result.success(rooms);
    }

    /** 根据楼栋ID获取所有宿舍列表（无过滤条件）
     * @param buildingId 楼栋ID
     * @return 宿舍列表 */
    @GetMapping("/all/building/{buildingId}")
    public Result<List<Room>> getAllByBuildingId(@PathVariable Long buildingId) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Room::getBuildingId, buildingId);
        List<Room> rooms = roomMapper.selectList(wrapper);
        return Result.success(rooms);
    }

    /** 获取楼栋下可用的宿舍列表（状态正常且未满员）
     * @param buildingId 楼栋ID
     * @return 可用宿舍列表 */
    @GetMapping("/available/{buildingId}")
    public Result<List<Room>> getAvailableRooms(@PathVariable Long buildingId) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Room::getBuildingId, buildingId);
        wrapper.eq(Room::getStatus, Constant.STATUS_NORMAL);
        wrapper.apply("current_count < bed_count");
        List<Room> rooms = roomMapper.selectList(wrapper);
        return Result.success(rooms);
    }

    /** 导入宿舍信息（从Excel文件批量导入）
     * @param file Excel文件
     * @return 操作结果 */
    @CacheEvict(value = "roomList", allEntries = true)
    @PostMapping("/import")
    public Result<Void> importRooms(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }
        
        try {
            List<Building> allBuildings = buildingMapper.selectList(null);
            Map<String, Long> buildingNameMap = new HashMap<>();
            for (Building b : allBuildings) {
                buildingNameMap.put(b.getBuildingName(), b.getId());
            }
            
            InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            
            List<Room> rooms = new ArrayList<>();
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                Cell cell0 = row.getCell(0);
                Cell cell1 = row.getCell(1);
                Cell cell2 = row.getCell(2);
                Cell cell3 = row.getCell(3);
                Cell cell4 = row.getCell(4);
                
                if (cell0 == null || getCellValue(cell0).isEmpty()) continue;
                
                String buildingName = getCellValue(cell0);
                Long buildingId = buildingNameMap.get(buildingName);
                if (buildingId == null) {
                    continue;
                }
                
                Room room = new Room();
                room.setBuildingId(buildingId);
                room.setRoomNumber(getCellValue(cell1));
                room.setFloor(getIntValue(cell2));
                room.setRoomType(getIntValue(cell3));
                room.setBedCount(getIntValue(cell4));
                room.setCurrentCount(0);
                room.setStatus(Constant.STATUS_NORMAL);
                
                rooms.add(room);
            }
            
            workbook.close();
            inputStream.close();
            
            if (!rooms.isEmpty()) {
                for (Room room : rooms) {
                    roomMapper.insert(room);
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
}
