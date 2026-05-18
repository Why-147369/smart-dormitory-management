package com.dormitory.controller;

import com.dormitory.common.Result;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RoleController - 角色控制器，负责管理系统角色信息
 * @author 王和友
 * @since 2026
 */
@RestController
@RequestMapping("/api/role")
public class RoleController {

    /**
     * 获取系统角色列表
     * @return 系统所有角色的详细信息列表，包含角色ID、名称、键值、描述和创建时间
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        List<Map<String, Object>> roles = new ArrayList<>();
        
        Map<String, Object> admin = new HashMap<>();
        admin.put("id", 1);
        admin.put("roleName", "管理员");
        admin.put("roleKey", "admin");
        admin.put("description", "系统管理员，拥有所有权限");
        admin.put("createTime", "2024-01-01 10:00:00");
        roles.add(admin);
        
        Map<String, Object> manager = new HashMap<>();
        manager.put("id", 2);
        manager.put("roleName", "宿管员");
        manager.put("roleKey", "manager");
        manager.put("description", "宿舍管理员，负责楼栋管理");
        manager.put("createTime", "2024-01-01 10:00:00");
        roles.add(manager);
        
        Map<String, Object> student = new HashMap<>();
        student.put("id", 3);
        student.put("roleName", "学生");
        student.put("roleKey", "student");
        student.put("description", "学生用户，基本操作权限");
        student.put("createTime", "2024-01-01 10:00:00");
        roles.add(student);
        
        return Result.success(roles);
    }
}
