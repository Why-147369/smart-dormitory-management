package com.dormitory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.common.Result;
import com.dormitory.entity.Building;
import com.dormitory.entity.DormitoryManager;
import com.dormitory.mapper.BuildingMapper;
import com.dormitory.mapper.DormitoryManagerMapper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** BuildingController - 楼栋管理控制器，提供楼栋的增删改查等操作
 * @author 王和友 @since 2026 */
@RestController
@RequestMapping("/api/building")
public class BuildingController {

    private final BuildingMapper buildingMapper;
    private final DormitoryManagerMapper managerMapper;

    public BuildingController(BuildingMapper buildingMapper, DormitoryManagerMapper managerMapper) {
        this.buildingMapper = buildingMapper;
        this.managerMapper = managerMapper;
    }

    /** 获取楼栋列表，返回所有楼栋信息及对应的宿管信息
     * @return 包含楼栋和宿管信息的列表 */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        List<Building> buildings = buildingMapper.selectList(null);
        
        List<Long> managerIds = buildings.stream()
            .map(Building::getManagerId)
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());
        
        Map<Long, DormitoryManager> managerMap = new HashMap<>();
        if (!managerIds.isEmpty()) {
            List<DormitoryManager> managers = managerMapper.selectBatchIds(managerIds);
            for (DormitoryManager m : managers) {
                managerMap.put(m.getId(), m);
            }
        }
        
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Building b : buildings) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());
            map.put("buildingNumber", b.getBuildingNumber());
            map.put("buildingName", b.getBuildingName());
            map.put("floorCount", b.getFloorCount());
            map.put("roomCount", b.getRoomCount());
            map.put("managerId", b.getManagerId());
            map.put("createTime", b.getCreateTime());
            
            if (b.getManagerId() != null) {
                DormitoryManager m = managerMap.get(b.getManagerId());
                if (m != null) {
                    map.put("managerName", m.getName());
                    map.put("phone", m.getPhone());
                }
            }
            result.add(map);
        }
        return Result.success(result);
    }

    /** 分页获取楼栋列表
     * @param pageNum 页码，默认1
     * @param pageSize 每页条数，默认10
     * @return 分页后的楼栋列表 */
    @GetMapping("/page")
    public Result<Page<Building>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Building> page = new Page<>(pageNum, pageSize);
        Page<Building> result = buildingMapper.selectPage(page, null);
        return Result.success(result);
    }

    /** 根据ID获取楼栋信息
     * @param id 楼栋ID
     * @return 楼栋详细信息 */
    @GetMapping("/{id}")
    public Result<Building> getById(@PathVariable Long id) {
        Building building = buildingMapper.selectById(id);
        return Result.success(building);
    }

    /** 添加新楼栋
     * @param building 楼栋信息
     * @return 操作结果 */
    @PostMapping
    public Result<Void> add(@RequestBody Building building) {
        if (building.getRoomCount() == null) {
            building.setRoomCount(0);
        }
        if (building.getFloorCount() == null) {
            building.setFloorCount(0);
        }
        buildingMapper.insert(building);
        return Result.success();
    }

    /** 更新楼栋信息
     * @param building 楼栋信息
     * @return 操作结果 */
    @PutMapping
    public Result<Void> update(@RequestBody Building building) {
        buildingMapper.updateById(building);
        return Result.success();
    }

    /** 删除楼栋
     * @param id 楼栋ID
     * @return 操作结果 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        buildingMapper.deleteById(id);
        return Result.success();
    }

    /** 根据宿管ID获取负责的楼栋
     * @param managerId 宿管ID
     * @return 楼栋信息 */
    @GetMapping("/manager/{managerId}")
    public Result<Building> getByManagerId(@PathVariable Long managerId) {
        LambdaQueryWrapper<Building> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Building::getManagerId, managerId);
        Building building = buildingMapper.selectOne(wrapper);
        return Result.success(building);
    }
}
