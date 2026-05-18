package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * OperationLogMapper - 操作日志Mapper
 * 对应OperationLog实体，用于处理系统操作日志相关数据，包括操作记录、操作审计等功能
 * @author 王和友
 * @since 2026
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
