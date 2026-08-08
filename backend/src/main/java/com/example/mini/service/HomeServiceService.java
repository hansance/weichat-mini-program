package com.example.mini.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mini.entity.HomeService;
import com.example.mini.mapper.HomeServiceMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 家政服务业务层
 */
@Service
public class HomeServiceService {

    @Resource
    private HomeServiceMapper homeServiceMapper;

    /**
     * 查询上架的服务列表
     */
    public List<HomeService> listAvailable(String category) {
        LambdaQueryWrapper<HomeService> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HomeService::getStatus, 1);
        if (category != null && !category.isEmpty()) {
            wrapper.eq(HomeService::getCategory, category);
        }
        wrapper.orderByDesc(HomeService::getCreateTime);
        return homeServiceMapper.selectList(wrapper);
    }

    /**
     * 分页查询服务
     */
    public Page<HomeService> page(int pageNum, int pageSize, String category) {
        Page<HomeService> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HomeService> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HomeService::getStatus, 1);
        if (category != null && !category.isEmpty()) {
            wrapper.eq(HomeService::getCategory, category);
        }
        wrapper.orderByDesc(HomeService::getCreateTime);
        return homeServiceMapper.selectPage(page, wrapper);
    }

    /**
     * 根据ID查询服务详情
     */
    public HomeService getById(Long id) {
        return homeServiceMapper.selectById(id);
    }
}
