package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.Building;
import org.apache.ibatis.annotations.Mapper;

/**
 * BuildingMapper - 楼栋Mapper接口，用于对楼栋信息进行数据库操作
 * 对应Entity：Building
 * 主要功能：提供楼栋的增删改查基本操作
 * @author 王和友
 * @since 2026
 */
@Mapper
public interface BuildingMapper extends BaseMapper<Building> {
}
