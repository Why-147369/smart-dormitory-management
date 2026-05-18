package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.DormitoryRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * DormitoryRuleMapper - 宿舍规则Mapper
 * 对应DormitoryRule实体，用于处理宿舍规章制度相关数据，包括规则发布、规则管理等功能
 * @author 王和友
 * @since 2026
 */
@Mapper
public interface DormitoryRuleMapper extends BaseMapper<DormitoryRule> {
}
