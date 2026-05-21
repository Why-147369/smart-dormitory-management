package com.dormitory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dormitory.common.Result;
import com.dormitory.entity.UtilityThreshold;
import com.dormitory.mapper.UtilityThresholdMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * UtilityThresholdController - 水电费阈值控制器
 * 提供水电费限额阈值配置管理的RESTful接口
 * @author 王和友
 * @since 2026
 */
@RestController
@RequestMapping("/api/utility/threshold")
public class UtilityThresholdController {

    private final UtilityThresholdMapper utilityThresholdMapper;

    public UtilityThresholdController(UtilityThresholdMapper utilityThresholdMapper) {
        this.utilityThresholdMapper = utilityThresholdMapper;
    }

    /**
     * 获取所有水电费阈值配置列表
     * @return 阈值配置列表
     */
    @Cacheable(value = "thresholdList")
    @GetMapping("/list")
    public Result<List<UtilityThreshold>> list() {
        List<UtilityThreshold> list = utilityThresholdMapper.selectList(null);
        return Result.success(list);
    }

    /**
     * 根据ID获取水电费阈值配置
     * @param id 阈值配置ID
     * @return 阈值配置详情
     */
    @GetMapping("/{id}")
    public Result<UtilityThreshold> getById(@PathVariable Long id) {
        UtilityThreshold threshold = utilityThresholdMapper.selectById(id);
        return Result.success(threshold);
    }

    /**
     * 根据房间类型获取水电费阈值配置
     * @param roomType 房间类型
     * @return 阈值配置详情
     */
    @GetMapping("/roomType/{roomType}")
    public Result<UtilityThreshold> getByRoomType(@PathVariable Integer roomType) {
        LambdaQueryWrapper<UtilityThreshold> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UtilityThreshold::getRoomType, roomType);
        UtilityThreshold threshold = utilityThresholdMapper.selectOne(wrapper);
        return Result.success(threshold);
    }

    /**
     * 新增水电费阈值配置
     * @param threshold 阈值配置对象
     * @return 操作结果
     */
    @CacheEvict(value = "thresholdList", allEntries = true)
    @PostMapping
    public Result<Void> add(@RequestBody UtilityThreshold threshold) {
        utilityThresholdMapper.insert(threshold);
        return Result.success();
    }

    /**
     * 更新水电费阈值配置
     * @param threshold 阈值配置对象
     * @return 操作结果
     */
    @CacheEvict(value = "thresholdList", allEntries = true)
    @PutMapping
    public Result<Void> update(@RequestBody UtilityThreshold threshold) {
        utilityThresholdMapper.updateById(threshold);
        return Result.success();
    }

    /**
     * 删除水电费阈值配置
     * @param id 阈值配置ID
     * @return 操作结果
     */
    @CacheEvict(value = "thresholdList", allEntries = true)
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        utilityThresholdMapper.deleteById(id);
        return Result.success();
    }
}
