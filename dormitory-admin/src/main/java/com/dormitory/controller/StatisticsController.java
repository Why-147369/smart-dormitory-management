package com.dormitory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dormitory.common.Result;
import com.dormitory.entity.*;
import com.dormitory.mapper.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * StatisticsController - 数据统计控制器，提供宿舍管理相关的各类统计数据
 * @author 王和友
 * @since 2026
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StudentMapper studentMapper;
    private final BuildingMapper buildingMapper;
    private final RoomMapper roomMapper;
    private final BedMapper bedMapper;
    private final RepairMapper repairMapper;
    private final RepairTypeMapper repairTypeMapper;

    public StatisticsController(StudentMapper studentMapper,
                                BuildingMapper buildingMapper,
                                RoomMapper roomMapper,
                                BedMapper bedMapper,
                                RepairMapper repairMapper,
                                RepairTypeMapper repairTypeMapper) {
        this.studentMapper = studentMapper;
        this.buildingMapper = buildingMapper;
        this.roomMapper = roomMapper;
        this.bedMapper = bedMapper;
        this.repairMapper = repairMapper;
        this.repairTypeMapper = repairTypeMapper;
    }

    /**
     * 获取宿舍管理的综合统计数据
     * @return 包含学生数量、楼栋数量、房间数量、入住率、各楼栋入住情况、维修类型分布等数据
     */
    @GetMapping
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> result = new HashMap<>();

        Long studentCount = studentMapper.selectCount(null);
        result.put("studentCount", studentCount);

        Long buildingCount = buildingMapper.selectCount(null);
        result.put("buildingCount", buildingCount);

        Long roomCount = roomMapper.selectCount(null);
        result.put("roomCount", roomCount);

        Long totalBeds = bedMapper.selectCount(null);
        Long occupiedBeds = bedMapper.selectCount(new LambdaQueryWrapper<Bed>()
                .isNotNull(Bed::getStudentId));
        double checkInRate = totalBeds > 0 ? (double) occupiedBeds / totalBeds * 100 : 0;
        checkInRate = Math.round(checkInRate * 100.0) / 100.0;
        result.put("checkInRate", checkInRate);

        List<Map<String, Object>> occupancyList = new ArrayList<>();
        List<Building> buildings = buildingMapper.selectList(null);
        for (Building building : buildings) {
            Map<String, Object> item = new HashMap<>();
            item.put("buildingName", building.getBuildingName());
            
            LambdaQueryWrapper<Room> roomWrapper = new LambdaQueryWrapper<>();
            roomWrapper.eq(Room::getBuildingId, building.getId());
            List<Room> rooms = roomMapper.selectList(roomWrapper);
            List<Long> roomIds = rooms.stream().map(Room::getId).collect(Collectors.toList());
            
            item.put("roomCount", rooms.size());
            
            long occupiedBedCount = 0;
            if (!roomIds.isEmpty()) {
                occupiedBedCount = bedMapper.selectCount(new LambdaQueryWrapper<Bed>()
                        .in(Bed::getRoomId, roomIds)
                        .isNotNull(Bed::getStudentId));
            }
            
            item.put("occupiedCount", occupiedBedCount);
            
            occupancyList.add(item);
        }
        result.put("occupancyList", occupancyList);

        List<RepairType> repairTypes = repairTypeMapper.selectList(
                new LambdaQueryWrapper<RepairType>().orderByAsc(RepairType::getSortOrder)
        );
        List<Map<String, Object>> repairTypeList = new ArrayList<>();
        for (RepairType type : repairTypes) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", type.getTypeName());
            Long count = repairMapper.selectCount(new LambdaQueryWrapper<Repair>()
                    .eq(Repair::getTypeId, type.getId()));
            item.put("value", count);
            repairTypeList.add(item);
        }
        result.put("repairTypeList", repairTypeList);

        return Result.success(result);
    }
}
