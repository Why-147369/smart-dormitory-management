package com.dormitory.mapper;

/**
 * UtilityThresholdMapper - 水电费阈值Mapper
 * 对应UtilityThreshold实体，负责水电费阈值的数据库操作
 * @author 王和友
 * @since 2026
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.UtilityThreshold;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UtilityThresholdMapper extends BaseMapper<UtilityThreshold> {
}
