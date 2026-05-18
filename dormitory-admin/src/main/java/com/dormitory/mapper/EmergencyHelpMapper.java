package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.EmergencyHelp;
import org.apache.ibatis.annotations.Mapper;

/**
 * EmergencyHelpMapper - 紧急求助Mapper
 * 对应EmergencyHelp实体，用于处理紧急求助相关数据，包括求助发起、求助处理等功能
 * @author 王和友
 * @since 2026
 */
@Mapper
public interface EmergencyHelpMapper extends BaseMapper<EmergencyHelp> {
}
