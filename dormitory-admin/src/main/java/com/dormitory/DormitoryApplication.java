package com.dormitory;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DormitoryApplication - 智能宿舍管理系统启动类
 * 
 * Spring Boot应用程序入口点，启动后端服务
 * 
 * @author 王和友
 * @since 2026
 */
@SpringBootApplication
@MapperScan("com.dormitory.mapper")
public class DormitoryApplication {
    /**
     * 应用程序入口方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(DormitoryApplication.class, args);
    }
}
