package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.HealthCheck;
import org.apache.ibatis.annotations.Mapper;

/**
 * HealthCheckMapper - 健康打卡Mapper
 * 对应HealthCheck实体，用于处理学生健康打卡相关数据，包括每日健康上报、体温记录等功能
 * @author 王和友
 * @since 2026
 */
@Mapper
public interface HealthCheckMapper extends BaseMapper<HealthCheck> {
}
