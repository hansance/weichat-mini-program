package com.example.mini.controller;

import com.example.mini.common.Result;
import com.example.mini.entity.Order;
import com.example.mini.service.OrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 订单接口
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Result<Order> create(@RequestBody Order order) {
        try {
            Order created = orderService.createOrder(order);
            return Result.success(created);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询我的订单
     */
    @GetMapping("/my")
    public Result<List<Order>> myOrders(@RequestParam String openId) {
        List<Order> orders = orderService.listByOpenId(openId);
        return Result.success(orders);
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id, @RequestParam String openId) {
        try {
            orderService.cancelOrder(id, openId);
            return Result.success(null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
