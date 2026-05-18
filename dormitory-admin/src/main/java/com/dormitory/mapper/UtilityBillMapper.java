package com.dormitory.mapper;

/**
 * UtilityBillMapper - 水电费账单Mapper
 * 对应UtilityBill实体，负责水电费账单的数据库操作
 * @author 王和友
 * @since 2026
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.UtilityBill;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UtilityBillMapper extends BaseMapper<UtilityBill> {
}
