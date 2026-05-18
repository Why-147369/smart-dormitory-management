package com.dormitory.mapper;

/**
 * UtilityWarningMapper - 水电费预警Mapper
 * 对应UtilityWarning实体，负责水电费预警的数据库操作
 * @author 王和友
 * @since 2026
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.UtilityWarning;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UtilityWarningMapper extends BaseMapper<UtilityWarning> {
}
