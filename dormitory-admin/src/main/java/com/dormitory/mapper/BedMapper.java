package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.entity.Bed;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * BedMapper - 床位Mapper接口，用于对床位信息进行数据库操作
 * 对应Entity：Bed
 * 主要功能：提供床位的增删改查基本操作，以及分页查询和查询所有床位（含关联宿舍、楼栋、学生信息）
 * @author 王和友
 * @since 2026
 */
@Mapper
public interface BedMapper extends BaseMapper<Bed> {
    
    @Select("<script>" +
            "SELECT b.id, b.room_id as roomId, b.bed_number as bedNumber, b.status, b.student_id as studentId, " +
            "r.room_number as roomNumber, r.building_id as buildingId, " +
            "bu.building_name as buildingName, " +
            "s.name as studentName " +
            "FROM bed b " +
            "LEFT JOIN room r ON b.room_id = r.id " +
            "LEFT JOIN building bu ON r.building_id = bu.id " +
            "LEFT JOIN student s ON b.student_id = s.id " +
            "<where>" +
            "<if test='roomId != null'> and b.room_id = #{roomId}</if>" +
            "<if test='buildingId != null'> and r.building_id = #{buildingId}</if>" +
            "<if test='status != null'> and b.status = #{status}</if>" +
            "</where>" +
            "ORDER BY b.id DESC" +
            "</script>")
    IPage<Map<String, Object>> selectBedPage(Page<Map<String, Object>> page, 
                                              @Param("buildingId") Long buildingId, 
                                              @Param("roomId") Long roomId,
                                              @Param("status") Integer status);
    
    @Select("<script>" +
            "SELECT b.id, b.room_id as roomId, b.bed_number as bedNumber, b.status, b.student_id as studentId, " +
            "r.room_number as roomNumber, r.building_id as buildingId, " +
            "bu.building_name as buildingName, " +
            "s.name as studentName, s.student_number as studentNumber " +
            "FROM bed b " +
            "LEFT JOIN room r ON b.room_id = r.id " +
            "LEFT JOIN building bu ON r.building_id = bu.id " +
            "LEFT JOIN student s ON b.student_id = s.id " +
            "<where>" +
            "<if test='roomId != null'> and b.room_id = #{roomId}</if>" +
            "<if test='buildingId != null'> and r.building_id = #{buildingId}</if>" +
            "<if test='status != null'> and b.status = #{status}</if>" +
            "</where>" +
            "ORDER BY bu.building_name, r.room_number, b.bed_number" +
            "</script>")
    List<Map<String, Object>> selectAllBeds(@Param("buildingId") Long buildingId, 
                                              @Param("roomId") Long roomId,
                                              @Param("status") Integer status);
}
