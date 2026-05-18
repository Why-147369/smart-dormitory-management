package com.dormitory.controller;

import com.dormitory.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * SystemController - 系统管理控制器，负责系统维护功能
 * @author 王和友
 * @since 2026
 */
@RestController
@RequestMapping("/api/system")
public class SystemController {

    @Value("${spring.datasource.username:root}")
    private String dbUsername;
    
    @Value("${spring.datasource.password:}")
    private String dbPassword;

    /**
     * 备份数据库，将数据库导出为SQL文件
     * @return 备份结果
     */
    @PostMapping("/backup")
    public Result<Map<String, Object>> backup() {
        Map<String, Object> data = new HashMap<>();
        
        try {
            String dbName = "dormitory_system";
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String backupDir = System.getProperty("user.dir") + File.separator + "backups";
            
            File dir = new File(backupDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            String backupFileName = dbName + "_" + timestamp + ".sql";
            String backupPath = backupDir + File.separator + backupFileName;
            
            String mysqldumpPath = findMysqldump();
            if (mysqldumpPath == null) {
                return Result.error("未找到mysqldump，请手动备份数据库");
            }
            
            ProcessBuilder pb;
            if (dbPassword != null && !dbPassword.isEmpty()) {
                pb = new ProcessBuilder(mysqldumpPath, "-u" + dbUsername, "-p" + dbPassword, "--port=3307", dbName);
            } else {
                pb = new ProcessBuilder(mysqldumpPath, "-u" + dbUsername, "--port=3307", dbName);
            }
            pb.redirectErrorStream(true);
            pb.redirectOutput(new File(backupPath));
            
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                File backupFile = new File(backupPath);
                if (backupFile.exists() && backupFile.length() > 0) {
                    data.put("success", true);
                    data.put("message", "备份成功: " + backupFileName);
                    data.put("fileName", backupFileName);
                    data.put("filePath", backupPath);
                    data.put("fileSize", backupFile.length());
                    return Result.success(data);
                } else {
                    return Result.error("备份文件为空");
                }
            } else {
                return Result.error("备份失败，exit code: " + exitCode);
            }
        } catch (Exception e) {
            return Result.error("备份失败: " + e.getMessage());
        }
    }
    
    private String findMysqldump() {
        String[] possiblePaths = {
            "mysqldump",
            "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe",
            "C:\\Program Files (x86)\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe",
            "C:\\MySQL\\bin\\mysqldump.exe",
            "D:\\MySQL\\bin\\mysqldump.exe"
        };
        
        for (String path : possiblePaths) {
            try {
                ProcessBuilder pb = new ProcessBuilder(path, "--version");
                pb.redirectErrorStream(true);
                Process p = pb.start();
                if (p.waitFor() == 0) {
                    return path;
                }
            } catch (Exception e) {
                continue;
            }
        }
        return "mysqldump";
    }
}
