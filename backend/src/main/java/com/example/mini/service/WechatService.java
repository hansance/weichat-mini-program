package com.example.mini.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 微信登录服务
 */
@Service
public class WechatService {

    @Value("${wechat.mini.app-id}")
    private String appId;

    @Value("${wechat.mini.app-secret}")
    private String appSecret;

    private static final String LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session" +
            "?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    /**
     * 通过code获取openId
     *
     * @param code 小程序端wx.login获取的code
     * @return openId
     */
    public String getOpenId(String code) {
        String url = String.format(LOGIN_URL, appId, appSecret, code);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                JSONObject jsonObject = JSON.parseObject(result);

                if (jsonObject.containsKey("openid")) {
                    return jsonObject.getString("openid");
                } else {
                    throw new RuntimeException("微信登录失败: " + jsonObject.getString("errmsg"));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("请求微信接口失败", e);
        }
    }
}
