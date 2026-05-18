package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.CivilizedDormitory;
import org.apache.ibatis.annotations.Mapper;

/**
 * CivilizedDormitoryMapper - 文明宿舍Mapper
 * 对应CivilizedDormitory实体，用于处理文明宿舍评选相关数据，包括评选记录、评分等功能
 * @author 王和友
 * @since 2026
 */
@Mapper
public interface CivilizedDormitoryMapper extends BaseMapper<CivilizedDormitory> {
}
