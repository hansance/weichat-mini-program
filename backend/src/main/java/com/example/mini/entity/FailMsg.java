package com.example.mini.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息发送失败记录表
 */
@Data
@TableName("fail_msg")
public class FailMsg {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 消息唯一ID */
    private String msgId;

    /** 交换机 */
    private String exchange;

    /** 路由键 */
    private String routingKey;

    /** 消息体（JSON） */
    private String msgBody;

    /** 错误信息 */
    private String errorMsg;

    /** 已重试次数 */
    private Integer retryCount;

    /** 状态：0-待重发 1-重发成功 2-重发失败（达到最大重试） */
    private Integer status;

    /** 下次重试时间 */
    private LocalDateTime nextRetryTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
