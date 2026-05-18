package com.dormitory.config;

import com.dormitory.common.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

/**
 * GlobalExceptionHandler - 全局异常处理器
 * 
 * 统一处理系统中的各类异常，将异常信息封装为统一的Result响应格式返回给前端
 * 
 * @author 王和友
 * @since 2026
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数绑定异常
     * 
     * @param e 参数绑定异常
     * @return Result 错误响应
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        StringBuilder sb = new StringBuilder();
        for (FieldError error : e.getFieldErrors()) {
            sb.append(error.getDefaultMessage()).append("; ");
        }
        return Result.error(sb.toString());
    }

    /**
     * 处理非法参数异常
     * 
     * @param e 非法参数异常
     * @return Result 错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        e.printStackTrace();
        return Result.error(e.getMessage());
    }

    /**
     * 处理空指针异常
     * 
     * @param e 空指针异常
     * @return Result 错误响应
     */
    @ExceptionHandler(NullPointerException.class)
    public Result<Void> handleNullPointerException(NullPointerException e) {
        e.printStackTrace();
        return Result.error("空指针异常: " + e.getMessage());
    }

    /**
     * 处理所有未捕获的异常
     * 
     * @param e 异常
     * @return Result 错误响应
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        e.printStackTrace();
        String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
        return Result.error("服务器错误: " + msg);
    }
}
