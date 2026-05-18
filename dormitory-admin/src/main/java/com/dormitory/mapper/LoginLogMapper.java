package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * LoginLogMapper - 登录日志Mapper
 * 对应LoginLog实体，用于处理用户登录日志相关数据，包括登录记录、登录统计等功能
 * @author 王和友
 * @since 2026
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {
}
