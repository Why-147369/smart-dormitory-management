package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * RoomMapper - 宿舍Mapper接口，用于对宿舍信息进行数据库操作
 * 对应Entity：Room
 * 主要功能：提供宿舍的增删改查基本操作，以及分页查询（含关联楼栋信息）
 * @author 王和友
 * @since 2026
 */
@Mapper
public interface RoomMapper extends BaseMapper<Room> {
    
    @Select("<script>" +
            "SELECT r.id, r.building_id as buildingId, r.room_number as roomNumber, r.floor, r.room_type as roomType, " +
            "r.bed_count as bedCount, r.current_count as currentCount, r.status, r.create_time as createTime, " +
            "b.building_name as buildingName " +
            "FROM room r " +
            "LEFT JOIN building b ON r.building_id = b.id " +
            "<where>" +
            "<if test='buildingId != null'> and r.building_id = #{buildingId}</if>" +
            "<if test='status != null'>" +
            "<choose>" +
            "<when test='status == 0'> and r.current_count &lt; r.bed_count</when>" +
            "<when test='status == 1'> and r.current_count &gt;= r.bed_count</when>" +
            "</choose>" +
            "</if>" +
            "</where>" +
            "ORDER BY r.id DESC" +
            "</script>")
    IPage<Map<String, Object>> selectRoomPage(Page<Map<String, Object>> page, 
                                              @Param("buildingId") Long buildingId,
                                              @Param("status") Integer status);
}
