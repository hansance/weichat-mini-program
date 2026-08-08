package com.example.mini.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mini.config.RabbitMqConfig;
import com.example.mini.entity.HomeService;
import com.example.mini.entity.Order;
import com.example.mini.mapper.OrderMapper;
import com.example.mini.mq.OrderTimeoutMessage;
import com.example.mini.mq.RabbitClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 订单业务层
 */
@Slf4j
@Service
public class OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private HomeServiceService homeServiceService;

    @Resource
    private RabbitClient rabbitClient;

    /**
     * 创建订单
     * 下单成功后发送延迟消息，用于订单超时自动取消
     */
    public Order createOrder(Order order) {
        // 生成订单编号
        String orderNo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        order.setOrderNo(orderNo);

        // 填充服务信息
        HomeService service = homeServiceService.getById(order.getServiceId());
        if (service == null) {
            throw new RuntimeException("服务不存在");
        }
        order.setServiceName(service.getName());
        order.setAmount(service.getPrice());
        order.setStatus(0); // 待支付
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        orderMapper.insert(order);

        // 发送延迟消息：30分钟后检查是否超时未支付
        sendOrderTimeoutMessage(order);

        return order;
    }

    /**
     * 发送订单超时延迟消息
     */
    private void sendOrderTimeoutMessage(Order order) {
        OrderTimeoutMessage timeoutMsg = new OrderTimeoutMessage(
                order.getId(),
                order.getOrderNo(),
                order.getOpenId()
        );
        try {
            rabbitClient.sendDelayMsg(
                    RabbitMqConfig.ORDER_DELAY_EXCHANGE,
                    RabbitMqConfig.ORDER_DELAY_ROUTING_KEY,
                    timeoutMsg
            );
            log.info("[下单] 延迟取消消息已发送: orderId={}, orderNo={}", order.getId(), order.getOrderNo());
        } catch (Exception e) {
            // 消息发送失败不影响下单（已有重试+持久化兜底）
            log.error("[下单] 延迟消息发送异常: orderId={}, error={}", order.getId(), e.getMessage());
        }
    }

    /**
     * 查询用户订单列表
     */
    public List<Order> listByOpenId(String openId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOpenId, openId);
        wrapper.orderByDesc(Order::getCreateTime);
        return orderMapper.selectList(wrapper);
    }

    /**
     * 取消订单
     */
    public void cancelOrder(Long orderId, String openId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getOpenId().equals(openId)) {
            throw new RuntimeException("无权操作");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("当前订单状态不可取消");
        }
        order.setStatus(4);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }
}
