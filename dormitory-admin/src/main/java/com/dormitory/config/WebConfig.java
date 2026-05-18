package com.dormitory.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig - Web MVC配置类
 * 
 * 配置Web资源处理器，映射URL路径到本地文件系统，
 * 实现静态资源（上传文件）的访问
 * 
 * @author 王和友
 * @since 2026
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 添加静态资源处理器
     * 将/api/uploads/** URL映射到项目根目录下的uploads文件夹
     * 
     * @param registry 资源处理器注册表
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = System.getProperty("user.dir") + "/uploads/";
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }
}
