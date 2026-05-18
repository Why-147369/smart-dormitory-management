package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.CheckOut;
import org.apache.ibatis.annotations.Mapper;

/**
 * CheckOutMapper - 退宿Mapper
 * 对应CheckOut实体，用于处理学生退宿相关数据，包括退宿申请、退宿审批等功能
 * @author 王和友
 * @since 2026
 */
@Mapper
public interface CheckOutMapper extends BaseMapper<CheckOut> {
}
