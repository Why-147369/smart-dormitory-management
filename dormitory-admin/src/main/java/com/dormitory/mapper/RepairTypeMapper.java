package com.dormitory.mapper;

/**
 * RepairTypeMapper - 报修类型Mapper，对应RepairType实体，负责报修类型信息的数据库操作
 * @author 王和友
 * @since 2026
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.RepairType;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RepairTypeMapper extends BaseMapper<RepairType> {
}
