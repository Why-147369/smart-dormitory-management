package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.CheckIn;
import org.apache.ibatis.annotations.Mapper;

/**
 * CheckInMapper - 入住Mapper，对应CheckIn实体，负责学生入住登记的数据操作
 * @author 王和友
 * @since 2026
 */
@Mapper
public interface CheckInMapper extends BaseMapper<CheckIn> {
}
