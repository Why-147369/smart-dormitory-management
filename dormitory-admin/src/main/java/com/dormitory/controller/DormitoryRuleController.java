package com.dormitory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.common.Result;
import com.dormitory.entity.DormitoryRule;
import com.dormitory.mapper.DormitoryRuleMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * DormitoryRuleController - 宿舍规则控制器
 * 提供宿舍规章制度的新增、查询、修改、删除等功能
 * @author 王和友
 * @since 2026
 */
@RestController
@RequestMapping("/api/rule")
public class DormitoryRuleController {

    private final DormitoryRuleMapper dormitoryRuleMapper;

    public DormitoryRuleController(DormitoryRuleMapper dormitoryRuleMapper) {
        this.dormitoryRuleMapper = dormitoryRuleMapper;
    }

    /**
     * 获取宿舍规则列表
     * 查询所有宿舍规则，支持按楼栋筛选（返回指定楼栋的规则和通用规则）
     * @param buildingId 楼栋ID（可选）
     * @return 规则列表
     */
    @GetMapping("/list")
    public Result<List<DormitoryRule>> list(@RequestParam(required = false) Long buildingId) {
        LambdaQueryWrapper<DormitoryRule> wrapper = new LambdaQueryWrapper<>();
        if (buildingId != null) {
            wrapper.and(w -> w.eq(DormitoryRule::getBuildingId, buildingId)
                    .or().isNull(DormitoryRule::getBuildingId));
        }
        wrapper.orderByAsc(DormitoryRule::getRuleType);
        List<DormitoryRule> list = dormitoryRuleMapper.selectList(wrapper);
        return Result.success(list);
    }

    /**
     * 分页查询宿舍规则
     * 分页获取宿舍规则列表，支持按关键词和规则类型筛选
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param keyword 关键词（可选）
     * @param ruleType 规则类型（可选）
     * @return 规则分页数据
     */
    @GetMapping("/page")
    public Result<Page<DormitoryRule>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer ruleType) {
        
        Page<DormitoryRule> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DormitoryRule> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(DormitoryRule::getTitle, keyword);
        }
        if (ruleType != null) {
            wrapper.eq(DormitoryRule::getRuleType, ruleType);
        }
        
        wrapper.orderByDesc(DormitoryRule::getCreateTime);
        Page<DormitoryRule> result = dormitoryRuleMapper.selectPage(page, wrapper);
        return Result.success(result);
    }

    /**
     * 获取宿舍规则详情
     * 根据ID查询宿舍规则的详细信息
     * @param id 规则ID
     * @return 规则详情
     */
    @GetMapping("/{id}")
    public Result<DormitoryRule> getById(@PathVariable Long id) {
        DormitoryRule rule = dormitoryRuleMapper.selectById(id);
        return Result.success(rule);
    }

    /**
     * 新增宿舍规则
     * 添加新的宿舍规则
     * @param rule 规则信息
     * @return 操作结果
     */
    @PostMapping
    public Result<Void> add(@RequestBody DormitoryRule rule) {
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());
        dormitoryRuleMapper.insert(rule);
        return Result.success();
    }

    /**
     * 更新宿舍规则
     * 修改已有的宿舍规则信息
     * @param rule 规则信息
     * @return 操作结果
     */
    @PutMapping
    public Result<Void> update(@RequestBody DormitoryRule rule) {
        rule.setUpdateTime(LocalDateTime.now());
        dormitoryRuleMapper.updateById(rule);
        return Result.success();
    }

    /**
     * 删除宿舍规则
     * 批量删除指定的宿舍规则
     * @param ids 规则ID，多个用逗号分隔
     * @return 操作结果
     */
    @DeleteMapping("/{ids}")
    public Result<Void> delete(@PathVariable String ids) {
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .toList();
        dormitoryRuleMapper.deleteBatchIds(idList);
        return Result.success();
    }
}
