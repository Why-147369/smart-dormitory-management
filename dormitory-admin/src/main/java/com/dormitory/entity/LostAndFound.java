package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * LostAndFound - 失物招领实体，用于记录失物招领信息
 * @author 王和友
 * @since 2026
 */
@Data
@TableName("lost_and_found")
public class LostAndFound {
    
    /** 失物招领记录ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 类型：1-失物 2-招领 */
    private Integer type;
    
    /** 物品名称 */
    private String itemName;
    
    /** 物品分类 */
    private String itemType;
    
    /** 丢失时间或拾取时间 */
    private LocalDateTime lostTime;
    
    /** 丢失地点或拾取地点 */
    private String lostPlace;
    
    /** 物品描述 */
    private String description;
    
    /** 联系方式 */
    private String contact;
    
    /** 物品图片路径，多个图片用逗号分隔 */
    private String images;
    
    /** 状态：0-待认领 1-已认领 2-已过期 */
    private Integer status;
    
    /** 发布人ID */
    private Long publisherId;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
}
