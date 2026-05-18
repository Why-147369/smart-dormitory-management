package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.CheckInApply;
import org.apache.ibatis.annotations.Mapper;

/**
 * CheckInApplyMapper - 入住申请Mapper，对应CheckInApply实体，负责学生入住申请的数据操作
 * @author 王和友
 * @since 2026
 */
@Mapper
public interface CheckInApplyMapper extends BaseMapper<CheckInApply> {
}
