package com.example.springboot.service;


import com.example.springboot.config.DeepSeekConfig;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DeepSeekService {

    private final RestTemplate restTemplate;
    private final DeepSeekConfig config;

    public DeepSeekService(RestTemplate restTemplate, DeepSeekConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    public String chat(String userMessage) {
        try {
            // 使用 Map 构建请求体 - 简化版
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModel());
            requestBody.put("messages", List.of(
                    Map.of("role", "user", "content", userMessage)
            ));
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);
            requestBody.put("stream", false);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 调用 API
            ResponseEntity<Map> response = restTemplate.exchange(
                    config.getUrl(),
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            // 处理响应 - 使用 Map 提取数据
            Map<String, Object> responseBody = response.getBody();
            return extractResponseContent(responseBody);

        } catch (Exception e) {
            log.error("调用DeepSeek API失败: {}", e.getMessage());
            throw new RuntimeException("AI服务暂时不可用，请稍后重试");
        }
    }

    // 提取响应内容的方法
    private String extractResponseContent(Map<String, Object> responseBody) {
        if (responseBody == null) {
            throw new RuntimeException("API返回空响应");
        }

        // 检查是否有错误
        if (responseBody.containsKey("error")) {
            Map<String, Object> error = (Map<String, Object>) responseBody.get("error");
            String errorMessage = (String) error.get("message");
            throw new RuntimeException("API返回错误: " + errorMessage);
        }

        // 提取回复内容
        if (responseBody.containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                return (String) message.get("content");
            }
        }

        throw new RuntimeException("无法解析API响应");
    }

    // 支持多轮对话的方法
    public String chatWithHistory(List<Map<String, String>> messageHistory) {
        try {
            // 构建消息列表
            List<Map<String, String>> messages = new ArrayList<>();
            for (Map<String, String> msg : messageHistory) {
                messages.add(Map.of(
                        "role", msg.get("role"),
                        "content", msg.get("content")
                ));
            }

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModel());
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);
            requestBody.put("stream", false);

            // 设置请求头并调用API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    config.getUrl(),
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            return extractResponseContent(response.getBody());

        } catch (Exception e) {
            log.error("调用DeepSeek API失败: {}", e.getMessage());
            throw new RuntimeException("AI服务暂时不可用，请稍后重试");
        }
    }
}