package com.dormitory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dormitory.common.Result;
import com.dormitory.entity.RepairComment;
import com.dormitory.mapper.RepairCommentMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RepairCommentController - 报修评价控制器，负责管理报修工单的评价信息
 * @author 王和友
 * @since 2026
 */
@RestController
@RequestMapping("/api/repair/comment")
public class RepairCommentController {

    private final RepairCommentMapper repairCommentMapper;

    public RepairCommentController(RepairCommentMapper repairCommentMapper) {
        this.repairCommentMapper = repairCommentMapper;
    }

    /**
     * 添加报修评价
     * @param comment 评价信息，包含报修ID、评分、评价内容等
     * @return 添加成功后的评价信息
     */
    @PostMapping
    public Result<RepairComment> add(@RequestBody RepairComment comment) {
        comment.setCreateTime(LocalDateTime.now());
        repairCommentMapper.insert(comment);
        return Result.success(comment);
    }

    /**
     * 根据报修ID获取评价列表
     * @param repairId 报修工单ID
     * @return 对应报修工单的评价列表，按时间倒序排列
     */
    @GetMapping("/repair/{repairId}")
    public Result<List<RepairComment>> getByRepairId(@PathVariable Long repairId) {
        LambdaQueryWrapper<RepairComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepairComment::getRepairId, repairId);
        wrapper.orderByDesc(RepairComment::getCreateTime);
        List<RepairComment> list = repairCommentMapper.selectList(wrapper);
        return Result.success(list);
    }
}
