package com.example.mini.controller;

import com.example.mini.common.Result;
import com.example.mini.service.WechatService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信登录接口
 */
@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Resource
    private WechatService wechatService;

    /**
     * 微信小程序登录
     *
     * @param params 包含code的请求体
     * @return openId（实际项目中应返回自定义token）
     */
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        if (code == null || code.isEmpty()) {
            return Result.error("code不能为空");
        }

        try {
            String openId = wechatService.getOpenId(code);
            Map<String, String> result = new HashMap<>();
            result.put("openId", openId);
            // 实际项目中这里应该生成JWT token
            result.put("token", "mock-token-" + openId);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
