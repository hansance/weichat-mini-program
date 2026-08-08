package com.example.mini.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 */
@Data
@TableName("service_order")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单编号 */
    private String orderNo;

    /** 用户openid */
    private String openId;

    /** 服务ID */
    private Long serviceId;

    /** 服务名称 */
    private String serviceName;

    /** 订单金额 */
    private BigDecimal amount;

    /** 联系人姓名 */
    private String contactName;

    /** 联系电话 */
    private String contactPhone;

    /** 服务地址 */
    private String address;

    /** 预约时间 */
    private LocalDateTime appointmentTime;

    /** 订单状态：0-待支付 1-待服务 2-服务中 3-已完成 4-已取消 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
