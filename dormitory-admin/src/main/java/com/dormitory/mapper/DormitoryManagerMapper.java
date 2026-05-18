package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.DormitoryManager;
import org.apache.ibatis.annotations.Mapper;

/**
 * DormitoryManagerMapper - 宿管信息Mapper接口，对应DormitoryManager实体，提供宿管数据的增删改查操作
 * @author 王和友
 * @since 2026
 */
@Mapper
public interface DormitoryManagerMapper extends BaseMapper<DormitoryManager> {
}
