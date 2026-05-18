package com.dormitory.mapper;

/**
 * MaintenancePersonMapper - 维修人员Mapper，对应MaintenancePerson实体，负责维修人员信息的数据库操作
 * @author 王和友
 * @since 2026
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.MaintenancePerson;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MaintenancePersonMapper extends BaseMapper<MaintenancePerson> {
}
