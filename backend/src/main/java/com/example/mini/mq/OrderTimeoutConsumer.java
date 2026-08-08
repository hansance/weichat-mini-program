package com.example.mini.mq;

import com.alibaba.fastjson.JSON;
import com.example.mini.config.RabbitMqConfig;
import com.example.mini.entity.Order;
import com.example.mini.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * 订单超时取消消费者
 * 监听死信队列（order.cancel.queue），当延迟消息到期后自动消费
 */
@Slf4j
@Component
public class OrderTimeoutConsumer {

    @Resource
    private OrderMapper orderMapper;

    /**
     * 处理订单超时取消消息
     * 仅取消仍处于"待支付"状态的订单
     */
    @RabbitListener(queues = RabbitMqConfig.ORDER_CANCEL_QUEUE)
    public void handleOrderTimeout(Message message) {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("[订单超时] 收到延迟消息: {}", body);

        try {
            OrderTimeoutMessage timeoutMsg = JSON.parseObject(body, OrderTimeoutMessage.class);

            // 查询订单
            Order order = orderMapper.selectById(timeoutMsg.getOrderId());
            if (order == null) {
                log.warn("[订单超时] 订单不存在: orderId={}", timeoutMsg.getOrderId());
                return;
            }

            // 仅取消"待支付"状态的订单（status=0）
            if (order.getStatus() != 0) {
                log.info("[订单超时] 订单已非待支付状态，跳过: orderId={}, status={}",
                        order.getId(), order.getStatus());
                return;
            }

            // 取消订单
            order.setStatus(4); // 4-已取消
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);

            log.info("[订单超时] 订单已自动取消: orderId={}, orderNo={}",
                    order.getId(), order.getOrderNo());

        } catch (Exception e) {
            log.error("[订单超时] 处理消息异常: body={}, error={}", body, e.getMessage(), e);
            // 抛出异常让 RabbitMQ 重试（根据配置的 retry 策略）
            throw new RuntimeException("订单超时处理失败", e);
        }
    }
}
