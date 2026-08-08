package com.example.mini.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mini.common.Result;
import com.example.mini.entity.HomeService;
import com.example.mini.service.HomeServiceService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 家政服务接口
 */
@RestController
@RequestMapping("/api/service")
public class ServiceController {

    @Resource
    private HomeServiceService homeServiceService;

    /**
     * 获取服务列表
     */
    @GetMapping("/list")
    public Result<List<HomeService>> list(@RequestParam(required = false) String category) {
        List<HomeService> list = homeServiceService.listAvailable(category);
        return Result.success(list);
    }

    /**
     * 分页获取服务
     */
    @GetMapping("/page")
    public Result<Page<HomeService>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String category) {
        Page<HomeService> page = homeServiceService.page(pageNum, pageSize, category);
        return Result.success(page);
    }

    /**
     * 获取服务详情
     */
    @GetMapping("/detail/{id}")
    public Result<HomeService> detail(@PathVariable Long id) {
        HomeService service = homeServiceService.getById(id);
        if (service == null) {
            return Result.error("服务不存在");
        }
        return Result.success(service);
    }
}
