package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.Visitor;
import org.apache.ibatis.annotations.Mapper;

/**
 * VisitorMapper - 访客Mapper，对应Visitor实体，负责访客登记与访问记录的数据操作
 * @author 王和友
 * @since 2026
 */
@Mapper
public interface VisitorMapper extends BaseMapper<Visitor> {
}
