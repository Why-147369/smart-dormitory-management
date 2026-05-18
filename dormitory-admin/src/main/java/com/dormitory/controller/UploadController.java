package com.dormitory.controller;

import com.dormitory.common.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

/**
 * UploadController - 文件上传控制器，提供单文件和Base64编码文件上传功能
 * @author 王和友
 * @since 2026
 */
@RestController
@RequestMapping("/api")
public class UploadController {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    /**
     * 上传文件到服务器
     * @param file 待上传的文件对象
     * @return 上传成功后的文件访问URL
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }

        try {
            File uploadPath = new File(UPLOAD_DIR);
            if (!uploadPath.exists()) {
                uploadPath.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + suffix;
            
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.write(filePath, file.getBytes());
            
            String fileUrl = "/api/uploads/" + fileName;
            return Result.success(fileUrl);
        } catch (IOException e) {
            return Result.error("上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 上传Base64编码的文件到服务器
     * @param file 待上传的文件对象（前端Base64解码后）
     * @return 上传成功后的文件访问URL
     */
    @PostMapping("/upload/base64")
    public Result<String> uploadBase64(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }

        try {
            File uploadPath = new File(UPLOAD_DIR);
            if (!uploadPath.exists()) {
                uploadPath.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + suffix;
            
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.write(filePath, file.getBytes());
            
            String fileUrl = "/api/uploads/" + fileName;
            return Result.success(fileUrl);
        } catch (IOException e) {
            return Result.error("上传失败：" + e.getMessage());
        }
    }
}