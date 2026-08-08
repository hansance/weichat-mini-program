package com.example.mini.controller;

import com.example.mini.common.Result;
import com.example.mini.entity.Order;
import com.example.mini.service.OrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 订单接口（需要 JWT 认证）
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    /**
     * 创建订单
     * openId 从 token 中获取，不再由前端传入
     */
    @PostMapping("/create")
    public Result<Order> create(@RequestBody Order order, HttpServletRequest request) {
        String openId = (String) request.getAttribute("openId");
        order.setOpenId(openId);
        try {
            Order created = orderService.createOrder(order);
            return Result.success(created);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询我的订单
     * openId 从 token 中获取
     */
    @GetMapping("/my")
    public Result<List<Order>> myOrders(HttpServletRequest request) {
        String openId = (String) request.getAttribute("openId");
        List<Order> orders = orderService.listByOpenId(openId);
        return Result.success(orders);
    }

    /**
     * 取消订单
     * openId 从 token 中获取
     */
    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id, HttpServletRequest request) {
        String openId = (String) request.getAttribute("openId");
        try {
            orderService.cancelOrder(id, openId);
            return Result.success(null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
