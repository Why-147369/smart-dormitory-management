package com.dormitory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.common.Result;
import com.dormitory.entity.LostAndFound;
import com.dormitory.entity.Student;
import com.dormitory.mapper.LostAndFoundMapper;
import com.dormitory.mapper.StudentMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * LostAndFoundController - 失物招领控制器
 * 提供失物招领信息发布、查询、认领等功能
 * @author 王和友
 * @since 2026
 */
@RestController
@RequestMapping("/api/lost-found")
public class LostAndFoundController {

    private final LostAndFoundMapper lostAndFoundMapper;
    private final StudentMapper studentMapper;

    public LostAndFoundController(LostAndFoundMapper lostAndFoundMapper,
                                 StudentMapper studentMapper) {
        this.lostAndFoundMapper = lostAndFoundMapper;
        this.studentMapper = studentMapper;
    }

    /**
     * 发布失物招领信息
     * 学生发布丢失物品或拾取物品的信息
     * @param lostAndFound 失物招领信息
     * @return 操作结果
     */
    @PostMapping
    public Result<Void> add(@RequestBody LostAndFound lostAndFound) {
        lostAndFound.setStatus(0);
        lostAndFound.setCreateTime(LocalDateTime.now());
        lostAndFoundMapper.insert(lostAndFound);
        return Result.success();
    }

    /**
     * 获取失物招领列表
     * 查询失物招领信息，支持按类型、物品分类、状态、日期范围筛选
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param type 类型（0：失物招领，1：拾取物品）
     * @param itemType 物品分类
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 失物招领分页数据
     */
    @GetMapping("/list")
    public Result<Page<Map<String, Object>>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                                 @RequestParam(required = false) Integer type,
                                                 @RequestParam(required = false) String itemType,
                                                 @RequestParam(required = false) Integer status,
                                                 @RequestParam(required = false) String startDate,
                                                 @RequestParam(required = false) String endDate) {
        Page<LostAndFound> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<LostAndFound> wrapper = new LambdaQueryWrapper<>();
        if (type != null) {
            wrapper.eq(LostAndFound::getType, type);
        }
        if (itemType != null && !itemType.isEmpty()) {
            wrapper.eq(LostAndFound::getItemType, itemType);
        }
        if (status != null) {
            wrapper.eq(LostAndFound::getStatus, status);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(LostAndFound::getLostTime, LocalDateTime.parse(startDate + "T00:00:00"));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(LostAndFound::getLostTime, LocalDateTime.parse(endDate + "T23:59:59"));
        }
        wrapper.orderByDesc(LostAndFound::getCreateTime);
        
        Page<LostAndFound> result = lostAndFoundMapper.selectPage(page, wrapper);
        
        List<Map<String, Object>> records = new ArrayList<>();
        for (LostAndFound l : result.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", l.getId());
            map.put("type", l.getType());
            map.put("itemName", l.getItemName());
            map.put("itemType", l.getItemType());
            map.put("lostTime", l.getLostTime());
            map.put("lostPlace", l.getLostPlace());
            map.put("description", l.getDescription());
            map.put("contact", l.getContact());
            map.put("images", l.getImages());
            map.put("status", l.getStatus());
            map.put("publisherId", l.getPublisherId());
            map.put("createTime", l.getCreateTime());
            
            Student student = studentMapper.selectById(l.getPublisherId());
            if (student != null) {
                map.put("publisherName", student.getName());
                map.put("publisherNumber", student.getStudentNumber());
            }
            records.add(map);
        }
        
        Page<Map<String, Object>> resultPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        resultPage.setRecords(records);
        return Result.success(resultPage);
    }

    /**
     * 获取物品分类列表
     * 获取所有已使用的物品分类，用于筛选下拉框
     * @return 物品分类列表
     */
    @GetMapping("/types")
    public Result<List<String>> getItemTypes() {
        LambdaQueryWrapper<LostAndFound> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(LostAndFound::getItemType);
        wrapper.isNotNull(LostAndFound::getItemType);
        wrapper.groupBy(LostAndFound::getItemType);
        List<LostAndFound> list = lostAndFoundMapper.selectList(wrapper);
        List<String> types = new ArrayList<>();
        for (LostAndFound l : list) {
            if (l.getItemType() != null && !types.contains(l.getItemType())) {
                types.add(l.getItemType());
            }
        }
        if (types.isEmpty()) {
            types = Arrays.asList("证件", "电子产品", "衣物", "书籍", "钱包", "钥匙", "其他");
        }
        return Result.success(types);
    }

    /**
     * 更新失物招领信息
     * 修改已有的失物招领信息
     * @param id 失物招领ID
     * @param lostAndFound 失物招领信息
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody LostAndFound lostAndFound) {
        lostAndFound.setId(id);
        lostAndFoundMapper.updateById(lostAndFound);
        return Result.success();
    }

    /**
     * 删除失物招领信息
     * 删除指定的失物招领记录
     * @param id 失物招领ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        lostAndFoundMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 认领失物
     * 物品失主认领丢失的物品，标记为已认领状态
     * @param id 失物招领ID
     * @return 操作结果
     */
    @PutMapping("/claim/{id}")
    public Result<Void> claim(@PathVariable Long id) {
        LostAndFound lostAndFound = lostAndFoundMapper.selectById(id);
        lostAndFound.setStatus(1);
        lostAndFoundMapper.updateById(lostAndFound);
        return Result.success();
    }

    /**
     * 根据状态批量删除失物招领信息
     * 管理员根据状态删除一批失物招领记录
     * @param status 状态（0：待认领，1：已认领）
     * @return 删除的记录数
     */
    @DeleteMapping("/delete-by-status/{status}")
    public Result<Integer> deleteByStatus(@PathVariable Integer status) {
        LambdaQueryWrapper<LostAndFound> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LostAndFound::getStatus, status);
        int deleted = lostAndFoundMapper.delete(wrapper);
        return Result.success(deleted);
    }
}
