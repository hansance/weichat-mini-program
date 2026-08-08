package com.example.mini.controller;

import com.example.mini.common.Result;
import com.example.mini.entity.Order;
import com.example.mini.mapper.OrderMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 支付接口（开发环境模拟支付，需要 JWT 认证）
 * 生产环境应替换为真实的微信支付接口
 */
@RestController
@RequestMapping("/api/pay")
public class PayController {

    @Resource
    private OrderMapper orderMapper;

    /**
     * 模拟支付：直接将订单状态改为已支付（待服务）
     * openId 从 token 中获取
     */
    @PostMapping("/mock/{orderId}")
    public Result<String> mockPay(@PathVariable Long orderId, HttpServletRequest request) {
        String openId = (String) request.getAttribute("openId");
        Order order = orderMapper.selectById(orderId);

        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!order.getOpenId().equals(openId)) {
            return Result.error("无权操作");
        }
        if (order.getStatus() != 0) {
            return Result.error("当前订单状态不可支付");
        }

        order.setStatus(1); // 待服务
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        return Result.success("模拟支付成功");
    }
}
