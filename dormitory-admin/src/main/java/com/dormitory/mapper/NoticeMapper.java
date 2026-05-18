package com.dormitory.mapper;

/**
 * NoticeMapper - 通知Mapper
 * 对应Notice实体，负责通知的数据库操作
 * @author 王和友
 * @since 2026
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.Notice;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {
}
