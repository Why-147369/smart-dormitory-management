package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

/**
 * AdminMapper - 管理员信息Mapper接口，对应Admin实体，提供管理员数据的增删改查操作
 * @author 王和友
 * @since 2026
 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
}
