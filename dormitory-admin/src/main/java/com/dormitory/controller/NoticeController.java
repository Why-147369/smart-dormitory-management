package com.dormitory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.common.Result;
import com.dormitory.entity.Notice;
import com.dormitory.mapper.NoticeMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * NoticeController - 通知管理控制器，处理公告通知的发布、查询、置顶、删除等业务功能
 * @author 王和友
 * @since 2026
 */
@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    private final NoticeMapper noticeMapper;

    public NoticeController(NoticeMapper noticeMapper) {
        this.noticeMapper = noticeMapper;
    }

    /**
     * 发布通知公告 - 管理员创建新的通知公告
     * @param notice 通知信息，包含标题、内容、类型等
     * @return 返回创建的通知公告记录
     */
    @PostMapping
    public Result<Notice> add(@RequestBody Notice notice) {
        notice.setCreateTime(LocalDateTime.now());
        if (notice.getIsTop() == null) {
            notice.setIsTop(0);
        }
        noticeMapper.insert(notice);
        return Result.success(notice);
    }

    /**
     * 查询通知公告列表 - 分页查询通知公告，支持按类型、发布时间筛选
     * @param pageNum 页码，默认1
     * @param pageSize 每页数量，默认10
     * @param noticeType 通知类型（可选），用于筛选特定类型的通知
     * @param startTime 开始时间（可选），筛选创建时间大于等于此时间的通知
     * @param endTime 结束时间（可选），筛选创建时间小于等于此时间的通知
     * @return 返回分页的通知公告列表，按置顶和创建时间排序
     */
    @GetMapping("/list")
    public Result<Page<Notice>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                    @RequestParam(required = false) String noticeType,
                                    @RequestParam(required = false) String startTime,
                                    @RequestParam(required = false) String endTime) {
        Page<Notice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        if (noticeType != null && !noticeType.isEmpty()) {
            wrapper.eq(Notice::getNoticeType, noticeType);
        }
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(Notice::getCreateTime, LocalDateTime.parse(startTime + "T00:00:00"));
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le(Notice::getCreateTime, LocalDateTime.parse(endTime + "T23:59:59"));
        }
        wrapper.orderByDesc(Notice::getIsTop);
        wrapper.orderByDesc(Notice::getCreateTime);
        Page<Notice> result = noticeMapper.selectPage(page, wrapper);
        return Result.success(result);
    }

    /**
     * 查询所有通知公告 - 获取所有通知公告（不分页），按置顶和创建时间排序
     * @return 返回所有通知公告列表
     */
    @GetMapping("/all")
    public Result<List<Notice>> all() {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Notice::getIsTop);
        wrapper.orderByDesc(Notice::getCreateTime);
        List<Notice> list = noticeMapper.selectList(wrapper);
        return Result.success(list);
    }

    /**
     * 根据ID查询通知公告 - 获取指定通知公告的详细信息
     * @param id 通知公告ID
     * @return 返回通知公告详情
     */
    @GetMapping("/{id}")
    public Result<Notice> getById(@PathVariable Long id) {
        Notice notice = noticeMapper.selectById(id);
        return Result.success(notice);
    }

    /**
     * 更新通知公告 - 管理员修改通知公告内容
     * @param notice 通知信息，包含需要更新的字段
     * @return 返回操作结果
     */
    @PutMapping
    public Result<Void> update(@RequestBody Notice notice) {
        noticeMapper.updateById(notice);
        return Result.success();
    }

    /**
     * 设置通知公告置顶 - 管理员设置或取消通知公告的置顶状态
     * @param id 通知公告ID
     * @param isTop 是否置顶，0-不置顶 1-置顶
     * @return 返回操作结果
     */
    @PutMapping("/top/{id}")
    public Result<Void> setTop(@PathVariable Long id, @RequestParam Integer isTop) {
        Notice notice = noticeMapper.selectById(id);
        notice.setIsTop(isTop);
        noticeMapper.updateById(notice);
        return Result.success();
    }

    /**
     * 删除通知公告 - 管理员删除指定的公告通知
     * @param id 通知公告ID
     * @return 返回操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        noticeMapper.deleteById(id);
        return Result.success();
    }
}
