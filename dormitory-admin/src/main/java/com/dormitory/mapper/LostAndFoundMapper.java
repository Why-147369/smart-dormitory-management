package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.LostAndFound;
import org.apache.ibatis.annotations.Mapper;

/**
 * LostAndFoundMapper - 失物招领Mapper，对应LostAndFound实体，负责失物招领信息的数据操作
 * @author 王和友
 * @since 2026
 */
@Mapper
public interface LostAndFoundMapper extends BaseMapper<LostAndFound> {
}
