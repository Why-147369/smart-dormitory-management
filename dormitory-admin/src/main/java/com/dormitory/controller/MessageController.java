package com.dormitory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.common.Result;
import com.dormitory.entity.Message;
import com.dormitory.mapper.MessageMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MessageController - 消息管理控制器，处理系统消息的发送、查询、已读、未读统计等业务功能
 * @author 王和友
 * @since 2026
 */
@RestController
@RequestMapping("/api/message")
public class MessageController {

    private final MessageMapper messageMapper;

    public MessageController(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    /**
     * 发送消息 - 管理员向用户发送系统消息
     * @param message 消息内容，包含接收用户ID、用户类型、标题、内容等
     * @return 返回操作结果
     */
    @PostMapping("/send")
    public Result<Void> send(@RequestBody Message message) {
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);
        return Result.success();
    }

    /**
     * 查询消息列表 - 分页查询消息记录，支持按用户ID、用户类型、已读状态筛选
     * @param pageNum 页码，默认1
     * @param pageSize 每页数量，默认10
     * @param userId 用户ID（可选），用于筛选特定用户的消息
     * @param userType 用户类型（可选），1-管理员 2-学生 3-超级管理员
     * @param isRead 已读状态（可选），0-未读 1-已读
     * @return 返回分页的消息列表
     */
    @GetMapping("/list")
    public Result<Page<Message>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                     @RequestParam(required = false) Long userId,
                                     @RequestParam(required = false) Integer userType,
                                     @RequestParam(required = false) Integer isRead) {
        Long currentUserId = getCurrentUserId();
        Integer currentUserType = getCurrentUserType();
        
        if (userId == null) userId = currentUserId;
        
        Page<Message> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(Message::getUserId, userId);
        }
        if (userType != null) {
            wrapper.eq(Message::getUserType, userType);
        } else if (currentUserType != null) {
            wrapper.eq(Message::getUserType, currentUserType);
        }
        if (isRead != null) {
            wrapper.eq(Message::getIsRead, isRead);
        }
        wrapper.orderByDesc(Message::getCreateTime);
        Page<Message> result = messageMapper.selectPage(page, wrapper);
        return Result.success(result);
    }

    /**
     * 获取未读消息数量 - 统计当前用户或指定用户的未读消息数量
     * @param userId 用户ID（可选），默认为当前登录用户
     * @param userType 用户类型（可选），1-管理员 2-学生 3-超级管理员
     * @return 返回未读消息数量
     */
    @GetMapping("/unread/count")
    public Result<Integer> unreadCount(@RequestParam(required = false) Long userId,
                                       @RequestParam(required = false) Integer userType) {
        Long currentUserId = getCurrentUserId();
        Integer currentUserType = getCurrentUserType();
        
        if (userId == null) userId = currentUserId;
        
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(Message::getUserId, userId);
        }
        if (userType != null) {
            wrapper.eq(Message::getUserType, userType);
        } else if (currentUserType != null) {
            wrapper.eq(Message::getUserType, currentUserType);
        }
        wrapper.eq(Message::getIsRead, 0);
        Long count = messageMapper.selectCount(wrapper);
        return Result.success(count.intValue());
    }

    /**
     * 标记消息为已读 - 将指定消息设置为已读状态
     * @param id 消息ID
     * @return 返回操作结果
     */
    @PutMapping("/read/{id}")
    public Result<Void> read(@PathVariable Long id) {
        Message message = messageMapper.selectById(id);
        if (message != null) {
            message.setIsRead(1);
            messageMapper.updateById(message);
        }
        return Result.success();
    }

    /**
     * 标记所有消息为已读 - 将当前用户或指定用户的所有未读消息设置为已读状态
     * @param userId 用户ID（可选），默认为当前登录用户
     * @param userType 用户类型（可选），1-管理员 2-学生 3-超级管理员
     * @return 返回操作结果
     */
    @PutMapping("/read/all")
    public Result<Void> readAll(@RequestParam(required = false) Long userId,
                                @RequestParam(required = false) Integer userType) {
        Long currentUserId = getCurrentUserId();
        Integer currentUserType = getCurrentUserType();
        
        if (userId == null) userId = currentUserId;
        
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(Message::getUserId, userId);
        }
        if (userType != null) {
            wrapper.eq(Message::getUserType, userType);
        } else if (currentUserType != null) {
            wrapper.eq(Message::getUserType, currentUserType);
        }
        wrapper.eq(Message::getIsRead, 0);
        List<Message> messages = messageMapper.selectList(wrapper);
        messages.forEach(m -> {
            m.setIsRead(1);
            messageMapper.updateById(m);
        });
        return Result.success();
    }

    /**
     * 删除消息 - 删除指定的消息记录
     * @param id 消息ID
     * @return 返回操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        messageMapper.deleteById(id);
        return Result.success();
    }
    
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            return (Long) auth.getPrincipal();
        }
        return null;
    }
    
    private Integer getCurrentUserType() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null) {
            for (var authority : auth.getAuthorities()) {
                String role = authority.getAuthority();
                if (role.equals("ROLE_STUDENT")) return 2;
                if (role.equals("ROLE_MANAGER")) return 1;
                if (role.equals("ROLE_ADMIN")) return 3;
            }
        }
        return null;
    }
}
