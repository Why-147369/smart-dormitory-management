package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.Student;
import org.apache.ibatis.annotations.Mapper;

/**
 * StudentMapper - 学生信息Mapper接口，对应Student实体，提供学生数据的增删改查操作
 * @author 王和友
 * @since 2026
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}
