package com.example.springboot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信相关操作（占位实现）。
 * 实际项目中需要对接微信小程序的接口，这里先通过日志模拟发送订阅消息。
 */
@Service
public class WechatService {

    private static final Logger logger = LoggerFactory.getLogger(WechatService.class);

    /**
     * 构造微信模板消息的数据结构
     *
     * @param keyValues 交替出现的模板字段 key 与 value
     */
    public Map<String, Map<String, String>> buildMessageData(String... keyValues) {
        Map<String, Map<String, String>> data = new HashMap<>();
        if (keyValues == null) {
            return data;
        }
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            String key = keyValues[i];
            String value = keyValues[i + 1];
            if (key == null) {
                continue;
            }
            Map<String, String> valueMap = new HashMap<>();
            valueMap.put("value", value == null ? "" : value);
            data.put(key, valueMap);
        }
        return data;
    }

    /**
     * 发送订阅消息（占位实现，记录日志后返回 true）
     */
    public boolean sendSubscribeMessage(String openid,
                                        String templateId,
                                        String page,
                                        Map<String, Map<String, String>> templateData) {
        logger.info("模拟发送微信订阅消息: openid={}, templateId={}, page={}, data={}",
                openid, templateId, page, templateData);
        // 此处可调用微信接口，当前先返回 true
        return true;
    }
}
































