package com.dormitory.common;

/**
 * Constant - 系统常量类
 * 
 * 定义系统中使用的各种常量，包括用户类型、状态码等
 * 
 * @author 智能宿舍管理系统
 * @since 2026
 */
public class Constant {
    
    /** 用户类型：学生 */
    public static final Integer USER_TYPE_STUDENT = 1;
    
    /** 用户类型：宿管 */
    public static final Integer USER_TYPE_MANAGER = 2;
    
    /** 用户类型：管理员 */
    public static final Integer USER_TYPE_ADMIN = 3;
    /** 用户类型：维修人员 */
    public static final Integer USER_TYPE_MAINTENANCE = 4;

    /** 账号状态：正常 */
    public static final Integer STATUS_NORMAL = 1;
    
    /** 账号状态：禁用 */
    public static final Integer STATUS_DISABLED = 0;

    /** 床位状态：空闲 */
    public static final Integer BED_STATUS_EMPTY = 0;
    
    /** 床位状态：已占用 */
    public static final Integer BED_STATUS_OCCUPIED = 1;
    
    /** 床位状态：维修中 */
    public static final Integer BED_STATUS_REPAIR = 2;
    
    /** 床位状态：损坏 */
    public static final Integer BED_STATUS_DAMAGED = 3;

    /** 报修状态：待处理 */
    public static final Integer REPAIR_STATUS_PENDING = 0;
    
    /** 报修状态：已接单 */
    public static final Integer REPAIR_STATUS_ACCEPTED = 1;
    
    /** 报修状态：维修中 */
    public static final Integer REPAIR_STATUS_REPAIRING = 2;
    
    /** 报修状态：已完成 */
    public static final Integer REPAIR_STATUS_COMPLETED = 3;
    
    /** 报修状态：已取消 */
    public static final Integer REPAIR_STATUS_CANCELLED = 4;

    /** 入住状态：正常 */
    public static final Integer CHECK_IN_STATUS_NORMAL = 1;
    
    /** 入住状态：补签 */
    public static final Integer CHECK_IN_STATUS_SUPPLEMENT = 0;

    /** 换寝状态：待审批 */
    public static final Integer ROOM_CHANGE_STATUS_PENDING = 0;
    
    /** 换寝状态：已通过 */
    public static final Integer ROOM_CHANGE_STATUS_APPROVED = 1;
    
    /** 换寝状态：已拒绝 */
    public static final Integer ROOM_CHANGE_STATUS_REJECTED = 2;
    
    /** 换寝状态：已取消 */
    public static final Integer ROOM_CHANGE_STATUS_CANCELLED = 3;

    /** 退宿状态：待审批 */
    public static final Integer CHECK_OUT_STATUS_PENDING = 0;
    
    /** 退宿状态：已通过 */
    public static final Integer CHECK_OUT_STATUS_APPROVED = 1;
    
    /** 退宿状态：已拒绝 */
    public static final Integer CHECK_OUT_STATUS_REJECTED = 2;
    
    /** 退宿状态：已完成 */
    public static final Integer CHECK_OUT_STATUS_DONE = 3;
}
