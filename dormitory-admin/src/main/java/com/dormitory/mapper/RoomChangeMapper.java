package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.RoomChange;
import org.apache.ibatis.annotations.Mapper;

/**
 * RoomChangeMapper - 换寝Mapper，对应RoomChange实体，负责宿舍换寝相关的数据操作
 * @author 王和友
 * @since 2026
 */
@Mapper
public interface RoomChangeMapper extends BaseMapper<RoomChange> {
}
