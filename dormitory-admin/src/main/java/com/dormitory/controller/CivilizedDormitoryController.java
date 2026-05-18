package com.dormitory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.common.Result;
import com.dormitory.entity.Building;
import com.dormitory.entity.CivilizedDormitory;
import com.dormitory.entity.HealthCheck;
import com.dormitory.entity.Room;
import com.dormitory.mapper.BuildingMapper;
import com.dormitory.mapper.CivilizedDormitoryMapper;
import com.dormitory.mapper.HealthCheckMapper;
import com.dormitory.mapper.RoomMapper;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * CivilizedDormitoryController - 文明宿舍控制器
 * 提供文明宿舍评选、排名计算、榜单查询等功能
 * @author 王和友
 * @since 2026
 */
@RestController
@RequestMapping("/api/civilized")
public class CivilizedDormitoryController {

    private final CivilizedDormitoryMapper civilizedDormitoryMapper;
    private final HealthCheckMapper healthCheckMapper;
    private final RoomMapper roomMapper;
    private final BuildingMapper buildingMapper;

    public CivilizedDormitoryController(CivilizedDormitoryMapper civilizedDormitoryMapper,
                                     HealthCheckMapper healthCheckMapper,
                                     RoomMapper roomMapper,
                                     BuildingMapper buildingMapper) {
        this.civilizedDormitoryMapper = civilizedDormitoryMapper;
        this.healthCheckMapper = healthCheckMapper;
        this.roomMapper = roomMapper;
        this.buildingMapper = buildingMapper;
    }

    /**
     * 计算文明宿舍评选结果
     * 根据指定月份的卫生检查记录，计算各宿舍的总分并排名
     * @param year 年份（可选，默认当前年）
     * @param month 月份（可选，默认当前月）
     * @return 操作结果
     */
    @PostMapping("/calculate")
    public Result<Void> calculate(@RequestParam(required = false) Integer year,
                                 @RequestParam(required = false) Integer month) {
        LocalDate now = LocalDate.now();
        if (year == null) {
            year = now.getYear();
        }
        if (month == null) {
            month = now.getMonthValue();
        }

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        LambdaQueryWrapper<HealthCheck> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(HealthCheck::getCheckDate, startDate, endDate);
        List<HealthCheck> filteredChecks = healthCheckMapper.selectList(queryWrapper);

        Map<Long, List<HealthCheck>> roomChecks = new HashMap<>();
        for (HealthCheck check : filteredChecks) {
            roomChecks.computeIfAbsent(check.getRoomId(), k -> new ArrayList<>()).add(check);
        }

        for (Map.Entry<Long, List<HealthCheck>> entry : roomChecks.entrySet()) {
            Long roomId = entry.getKey();
            List<HealthCheck> roomCheckList = entry.getValue();

            BigDecimal totalScore = BigDecimal.ZERO;
            int checkCount = 0;
            for (HealthCheck check : roomCheckList) {
                totalScore = totalScore.add(BigDecimal.valueOf(check.getScore()));
                checkCount++;
            }
            BigDecimal avgScore = totalScore.divide(BigDecimal.valueOf(checkCount), 2, RoundingMode.HALF_UP);

            LambdaQueryWrapper<CivilizedDormitory> existWrapper = new LambdaQueryWrapper<>();
            existWrapper.eq(CivilizedDormitory::getRoomId, roomId);
            existWrapper.eq(CivilizedDormitory::getYear, year);
            existWrapper.eq(CivilizedDormitory::getMonth, month);
            CivilizedDormitory existing = civilizedDormitoryMapper.selectOne(existWrapper);

            if (existing != null) {
                existing.setTotalScore(totalScore);
                civilizedDormitoryMapper.updateById(existing);
            } else {
                CivilizedDormitory civilized = new CivilizedDormitory();
                civilized.setRoomId(roomId);
                civilized.setYear(year);
                civilized.setMonth(month);
                civilized.setTotalScore(totalScore);
                civilized.setCreateTime(LocalDateTime.now());
                civilizedDormitoryMapper.insert(civilized);
            }
        }

        LambdaQueryWrapper<CivilizedDormitory> rankWrapper = new LambdaQueryWrapper<>();
        rankWrapper.eq(CivilizedDormitory::getYear, year);
        rankWrapper.eq(CivilizedDormitory::getMonth, month);
        rankWrapper.orderByDesc(CivilizedDormitory::getTotalScore);
        List<CivilizedDormitory> all = civilizedDormitoryMapper.selectList(rankWrapper);

        int rank = 1;
        for (CivilizedDormitory c : all) {
            c.setRank(rank++);
            civilizedDormitoryMapper.updateById(c);
        }

        return Result.success();
    }

    /**
     * 获取文明宿舍排名列表
     * 查询指定月份的文明宿舍排名，支持按楼栋筛选
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param year 年份（可选）
     * @param month 月份（可选）
     * @param buildingId 楼栋ID（可选）
     * @return 文明宿舍排名分页数据
     */
    @GetMapping("/rank")
    public Result<Page<Map<String, Object>>> rank(@RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "100") Integer pageSize,
                                                 @RequestParam(required = false) Integer year,
                                                 @RequestParam(required = false) Integer month,
                                                 @RequestParam(required = false) Long buildingId) {
        LambdaQueryWrapper<CivilizedDormitory> wrapper = new LambdaQueryWrapper<>();
        if (year != null) {
            wrapper.eq(CivilizedDormitory::getYear, year);
        }
        if (month != null) {
            wrapper.eq(CivilizedDormitory::getMonth, month);
        }
        wrapper.orderByDesc(CivilizedDormitory::getTotalScore);
        
        List<CivilizedDormitory> allList = civilizedDormitoryMapper.selectList(wrapper);
        
        List<Map<String, Object>> filteredList = new ArrayList<>();
        for (CivilizedDormitory c : allList) {
            Room room = roomMapper.selectById(c.getRoomId());
            if (room != null) {
                if (buildingId != null && !buildingId.equals(room.getBuildingId())) {
                    continue;
                }
                Map<String, Object> map = new HashMap<>();
                map.put("id", c.getId());
                map.put("roomId", c.getRoomId());
                map.put("year", c.getYear());
                map.put("month", c.getMonth());
                map.put("totalScore", c.getTotalScore());
                map.put("rank", c.getRank());
                map.put("roomNumber", room.getRoomNumber());
                Building building = buildingMapper.selectById(room.getBuildingId());
                if (building != null) {
                    map.put("buildingId", building.getId());
                    map.put("buildingName", building.getBuildingName());
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
     * 获取文明宿舍TOP10
     * 查询指定月份排名前10的文明宿舍
     * @param year 年份（可选）
     * @param month 月份（可选）
     * @param buildingId 楼栋ID（可选）
     * @return TOP10文明宿舍列表
     */
    @GetMapping("/top10")
    public Result<List<Map<String, Object>>> top10(@RequestParam(required = false) Integer year,
                                                 @RequestParam(required = false) Integer month,
                                                 @RequestParam(required = false) Long buildingId) {
        LambdaQueryWrapper<CivilizedDormitory> wrapper = new LambdaQueryWrapper<>();
        if (year != null) {
            wrapper.eq(CivilizedDormitory::getYear, year);
        }
        if (month != null) {
            wrapper.eq(CivilizedDormitory::getMonth, month);
        }
        wrapper.orderByDesc(CivilizedDormitory::getTotalScore);
        List<CivilizedDormitory> allList = civilizedDormitoryMapper.selectList(wrapper);
        
        Set<Long> roomIds = new HashSet<>();
        Map<Long, Room> roomMap = new HashMap<>();
        if (buildingId != null) {
            LambdaQueryWrapper<Room> roomWrapper = new LambdaQueryWrapper<>();
            roomWrapper.eq(Room::getBuildingId, buildingId);
            List<Room> rooms = roomMapper.selectList(roomWrapper);
            for (Room r : rooms) {
                roomIds.add(r.getId());
                roomMap.put(r.getId(), r);
            }
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        int count = 0;
        for (CivilizedDormitory c : allList) {
            if (count >= 10) break;
            
            if (buildingId != null && !roomIds.contains(c.getRoomId())) {
                continue;
            }
            
            Room room = roomMap.containsKey(c.getRoomId()) ? roomMap.get(c.getRoomId()) : roomMapper.selectById(c.getRoomId());
            if (room != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", c.getId());
                map.put("roomId", c.getRoomId());
                map.put("year", c.getYear());
                map.put("month", c.getMonth());
                map.put("totalScore", c.getTotalScore());
                map.put("rank", c.getRank());
                map.put("roomNumber", room.getRoomNumber());
                Building building = buildingMapper.selectById(room.getBuildingId());
                if (building != null) {
                    map.put("buildingId", building.getId());
                    map.put("buildingName", building.getBuildingName());
                }
                result.add(map);
                count++;
            }
        }
        return Result.success(result);
    }

    /**
     * 获取各楼栋最优宿舍
     * 查询每个楼栋中评分最高的宿舍
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param buildingId 楼栋ID（可选）
     * @param year 年份（可选）
     * @param month 月份（可选）
     * @return 各楼栋最优宿舍分页数据
     */
    @GetMapping("/building-top")
    public Result<Page<Map<String, Object>>> buildingTop(@RequestParam(defaultValue = "1") Integer pageNum,
                                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                                         @RequestParam(required = false) Long buildingId,
                                                         @RequestParam(required = false) Integer year,
                                                         @RequestParam(required = false) Integer month) {
        LambdaQueryWrapper<CivilizedDormitory> wrapper = new LambdaQueryWrapper<>();
        if (year != null) {
            wrapper.eq(CivilizedDormitory::getYear, year);
        }
        if (month != null) {
            wrapper.eq(CivilizedDormitory::getMonth, month);
        }
        wrapper.orderByDesc(CivilizedDormitory::getTotalScore);
        List<CivilizedDormitory> allList = civilizedDormitoryMapper.selectList(wrapper);
        
        Map<Long, Map<String, Object>> buildingBest = new LinkedHashMap<>();
        for (CivilizedDormitory c : allList) {
            Room room = roomMapper.selectById(c.getRoomId());
            if (room != null) {
                Long roomBuildingId = room.getBuildingId();
                if (buildingId != null && !buildingId.equals(roomBuildingId)) {
                    continue;
                }
                if (!buildingBest.containsKey(roomBuildingId)) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("buildingId", roomBuildingId);
                    map.put("totalScore", c.getTotalScore());
                    map.put("roomId", c.getRoomId());
                    map.put("roomNumber", room.getRoomNumber());
                    map.put("year", c.getYear());
                    map.put("month", c.getMonth());
                    Building building = buildingMapper.selectById(roomBuildingId);
                    if (building != null) {
                        map.put("buildingName", building.getBuildingName());
                    }
                    buildingBest.put(roomBuildingId, map);
                }
            }
        }
        
        List<Map<String, Object>> allData = new ArrayList<>(buildingBest.values());
        int total = allData.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Map<String, Object>> pageList = start < total ? allData.subList(start, end) : new ArrayList<>();
        
        Page<Map<String, Object>> resultPage = new Page<>(pageNum, pageSize, total);
        resultPage.setRecords(pageList);
        return Result.success(resultPage);
    }

    /**
     * 删除文明宿舍记录
     * 删除指定的文明宿舍评选记录
     * @param id 文明宿舍记录ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        civilizedDormitoryMapper.deleteById(id);
        return Result.success();
    }
    
    /**
     * 批量删除文明宿舍记录
     * 批量删除多条文明宿舍评选记录
     * @param ids 文明宿舍记录ID列表
     * @return 操作结果
     */
    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        civilizedDormitoryMapper.deleteBatchIds(ids);
        return Result.success();
    }

    /**
     * 获取已有评比的年份列表
     * 获取所有已有文明宿舍评比的年份
     * @return 年份列表
     */
    @GetMapping("/years")
    public Result<List<Integer>> getYears() {
        LambdaQueryWrapper<CivilizedDormitory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(CivilizedDormitory::getYear);
        List<CivilizedDormitory> list = civilizedDormitoryMapper.selectList(wrapper);
        Set<Integer> yearSet = new LinkedHashSet<>();
        for (CivilizedDormitory c : list) {
            if (c.getYear() != null) {
                yearSet.add(c.getYear());
            }
        }
        return Result.success(new ArrayList<>(yearSet));
    }
}
