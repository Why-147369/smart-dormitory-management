package com.dormitory.mapper;

/**
 * RepairMapper - 报修Mapper，对应Repair实体，负责报修信息的数据库操作
 * @author 王和友
 * @since 2026
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.Repair;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RepairMapper extends BaseMapper<Repair> {
}
