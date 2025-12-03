package com.example.springboot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIService {

    private final RestTemplate restTemplate;

    @Value("${ai.model.api-url:http://localhost:8000}")
    private String modelApiUrl;

    public AIService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    /**
     * 发送消息到AI模型
     */
    public String chatWithModel(String message, String customApiUrl) {
        String apiUrl = StringUtils.hasText(customApiUrl) ? customApiUrl : modelApiUrl;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "qwen-model");
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 1024);
        requestBody.put("stop", List.of("<think>", "<|im_end|>"));
        requestBody.put("stream", false);

        try {
            String url = apiUrl + "/v1/chat/completions";
            Map<String, Object> response = restTemplate.postForObject(
                    url, requestBody, Map.class);

            // 解析响应
            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
                    return (String) messageObj.get("content");
                }
            }
            return "抱歉，AI没有返回有效响应";

        } catch (Exception e) {
            throw new RuntimeException("调用AI模型失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查模型服务是否健康
     */
    public boolean isModelHealthy(String customApiUrl) {
        try {
            String apiUrl = StringUtils.hasText(customApiUrl) ? customApiUrl : modelApiUrl;
            String healthUrl = apiUrl + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}