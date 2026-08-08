package com.example.mini.controller;

import com.example.mini.common.JwtUtil;
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

    @Resource
    private JwtUtil jwtUtil;

    /**
     * 微信小程序登录
     *
     * @param params 包含code的请求体
     * @return openId + JWT token
     */
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        if (code == null || code.isEmpty()) {
            return Result.error("code不能为空");
        }

        try {
            String openId = wechatService.getOpenId(code);
            String token = jwtUtil.generateToken(openId);

            Map<String, String> result = new HashMap<>();
            result.put("openId", openId);
            result.put("token", token);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
