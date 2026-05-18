package com.dormitory.common;

import lombok.Data;

/**
 * Result - 统一响应结果类
 * 
 * 用于封装API的响应数据，统一返回格式
 * 
 * @author 智能宿舍管理系统
 * @since 2026
 */
@Data
public class Result<T> {
    
    /** 状态码：200表示成功，500表示失败 */
    private Integer code;
    
    /** 提示信息 */
    private String message;
    
    /** 响应数据 */
    private T data;

    /**
     * 成功响应（无数据）
     * 
     * @return 成功的Result对象
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功响应（带数据）
     * 
     * @param data 响应数据
     * @return 成功的Result对象
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 成功响应（带自定义消息）
     * 
     * @param message 自定义消息
     * @param data 响应数据
     * @return 成功的Result对象
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 错误响应
     * 
     * @param message 错误消息
     * @return 错误的Result对象
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    /**
     * 错误响应（自定义状态码）
     * 
     * @param code 自定义状态码
     * @param message 错误消息
     * @return 错误的Result对象
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
