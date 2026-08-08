package com.example.mini.mq;

import com.alibaba.fastjson.JSON;
import com.example.mini.entity.FailMsg;
import com.example.mini.mapper.FailMsgMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * RabbitMQ 消息发送客户端
 * - 发送失败自动重试3次（指数退避）
 * - 重试全部失败后持久化到 fail_msg 表
 */
@Slf4j
@Component
public class RabbitClient {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private FailMsgMapper failMsgMapper;

    /**
     * 发送消息（简单版，无延迟）
     */
    public void sendMsg(String exchange, String routingKey, Object msg) {
        doSend(exchange, routingKey, msg, null);
    }

    /**
     * 发送延迟消息
     * 注意：延迟由队列 TTL 控制，此处仅将消息发送到延迟队列
     */
    public void sendDelayMsg(String exchange, String routingKey, Object msg) {
        doSend(exchange, routingKey, msg, null);
    }

    /**
     * 核心发送方法，带重试
     *
     * @param exchange   交换机
     * @param routingKey 路由键
     * @param msg        消息体（会序列化为 JSON）
     * @param msgId      消息ID（可为null，自动生成）
     */
    @Retryable(
            value = RuntimeException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 3000, multiplier = 1.5)
    )
    public void doSend(String exchange, String routingKey, Object msg, String msgId) {
        // 生成消息ID
        if (msgId == null) {
            msgId = UUID.randomUUID().toString().replace("-", "");
        }

        String jsonMsg = JSON.toJSONString(msg);
        log.info("[RabbitMQ] 发送消息: exchange={}, routingKey={}, msgId={}, body={}",
                exchange, routingKey, msgId, jsonMsg);

        // 构建消息
        Message message = MessageBuilder
                .withBody(jsonMsg.getBytes(StandardCharsets.UTF_8))
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .setMessageId(msgId)
                .build();

        // 关联数据（用于确认回调）
        CorrelationData correlationData = new CorrelationData(msgId);

        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
        } catch (Exception e) {
            log.error("[RabbitMQ] 消息发送异常: msgId={}, error={}", msgId, e.getMessage());
            throw new RuntimeException("消息发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 重试3次全部失败后的兜底方法
     * 将失败消息持久化到数据库，后续可通过定时任务重发
     */
    @Recover
    public void saveFailMsg(RuntimeException e, String exchange, String routingKey, Object msg, String msgId) {
        log.error("[RabbitMQ] 消息发送3次重试失败，持久化到数据库: exchange={}, routingKey={}, error={}",
                exchange, routingKey, e.getMessage());

        FailMsg failMsg = new FailMsg();
        failMsg.setMsgId(msgId != null ? msgId : UUID.randomUUID().toString().replace("-", ""));
        failMsg.setExchange(exchange);
        failMsg.setRoutingKey(routingKey);
        failMsg.setMsgBody(JSON.toJSONString(msg));
        failMsg.setErrorMsg(e.getMessage());
        failMsg.setRetryCount(3);
        failMsg.setStatus(0); // 0-待重发
        failMsg.setCreateTime(LocalDateTime.now());
        failMsg.setNextRetryTime(LocalDateTime.now().plusMinutes(5));

        failMsgMapper.insert(failMsg);
        log.info("[RabbitMQ] 失败消息已持久化: msgId={}", failMsg.getMsgId());
    }
}
