package com.example.mini.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单超时取消延迟消息体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderTimeoutMessage implements Serializable {

    /** 订单ID */
    private Long orderId;

    /** 订单编号 */
    private String orderNo;

    /** 用户openId */
    private String openId;
}
