package com.example.mini.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 配置类
 * 使用延迟队列（TTL + 死信队列）实现订单超时取消
 */
@Configuration
@EnableRetry
public class RabbitMqConfig {

    // ===== 交换机 =====
    /** 订单延迟交换机（普通交换机，消息带TTL） */
    public static final String ORDER_DELAY_EXCHANGE = "order.delay.exchange";
    /** 订单死信交换机（接收超时消息） */
    public static final String ORDER_DLX_EXCHANGE = "order.dlx.exchange";

    // ===== 队列 =====
    /** 订单延迟队列（消息在此等待超时） */
    public static final String ORDER_DELAY_QUEUE = "order.delay.queue";
    /** 订单取消队列（死信队列，消费者监听此队列） */
    public static final String ORDER_CANCEL_QUEUE = "order.cancel.queue";

    // ===== 路由键 =====
    public static final String ORDER_DELAY_ROUTING_KEY = "order.delay";
    public static final String ORDER_CANCEL_ROUTING_KEY = "order.cancel";

    /** 订单超时时间（毫秒），默认30分钟 */
    public static final int ORDER_TIMEOUT_MS = 30 * 60 * 1000;

    /**
     * JSON 消息转换器
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate 配置（启用 publisher confirm 和 return）
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        // 消息到达交换机确认
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                // 消息未到达交换机，由 RabbitClient 重试机制处理
                System.err.println("[RabbitMQ] 消息未到达交换机: " + cause);
            }
        });
        // 消息无法路由到队列时回调
        rabbitTemplate.setReturnsCallback(returned -> {
            System.err.println("[RabbitMQ] 消息无法路由: exchange=" + returned.getExchange()
                    + ", routingKey=" + returned.getRoutingKey()
                    + ", replyCode=" + returned.getReplyCode()
                    + ", reason=" + returned.getReplyText());
        });
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }

    // ===== 延迟队列方案：TTL + 死信队列 =====

    /**
     * 订单延迟交换机（Direct 类型）
     */
    @Bean
    public DirectExchange orderDelayExchange() {
        return new DirectExchange(ORDER_DELAY_EXCHANGE, true, false);
    }

    /**
     * 订单死信交换机（Direct 类型）
     */
    @Bean
    public DirectExchange orderDlxExchange() {
        return new DirectExchange(ORDER_DLX_EXCHANGE, true, false);
    }

    /**
     * 订单延迟队列
     * 设置 TTL 和死信交换机：消息超时后转发到死信交换机
     */
    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        // 队列消息过期时间
        args.put("x-message-ttl", ORDER_TIMEOUT_MS);
        // 死信交换机
        args.put("x-dead-letter-exchange", ORDER_DLX_EXCHANGE);
        // 死信路由键
        args.put("x-dead-letter-routing-key", ORDER_CANCEL_ROUTING_KEY);
        return new Queue(ORDER_DELAY_QUEUE, true, false, false, args);
    }

    /**
     * 订单取消队列（死信队列）
     */
    @Bean
    public Queue orderCancelQueue() {
        return new Queue(ORDER_CANCEL_QUEUE, true);
    }

    /**
     * 绑定：延迟交换机 → 延迟队列
     */
    @Bean
    public Binding orderDelayBinding() {
        return BindingBuilder.bind(orderDelayQueue())
                .to(orderDelayExchange())
                .with(ORDER_DELAY_ROUTING_KEY);
    }

    /**
     * 绑定：死信交换机 → 取消队列
     */
    @Bean
    public Binding orderCancelBinding() {
        return BindingBuilder.bind(orderCancelQueue())
                .to(orderDlxExchange())
                .with(ORDER_CANCEL_ROUTING_KEY);
    }
}
