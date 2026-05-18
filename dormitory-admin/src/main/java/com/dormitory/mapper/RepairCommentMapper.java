package com.dormitory.mapper;

/**
 * RepairCommentMapper - 报修评价Mapper，对应RepairComment实体，负责报修评价信息的数据库操作
 * @author 王和友
 * @since 2026
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.RepairComment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RepairCommentMapper extends BaseMapper<RepairComment> {
}
