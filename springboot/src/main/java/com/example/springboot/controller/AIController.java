package com.example.springboot.controller;

import com.example.springboot.dto.ChatRequest;
import com.example.springboot.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ai.model.api-url:http://localhost:8000}")
    private String modelApiUrl;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<String>> chat(@RequestBody ChatRequest request) {
        System.out.println("=== 收到聊天请求 ===");
        System.out.println("请求内容: " + request);

        try {
            String response = callAIModel(request.getMessage(), request.getApiUrl());
            System.out.println("最终返回给客户端的响应: " + response);
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {

            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("AI服务暂时不可用: " + e.getMessage()));
        }
    }

    private String callAIModel(String message, String customApiUrl) {
        try {
            String apiUrl = customApiUrl != null && !customApiUrl.trim().isEmpty()
                    ? customApiUrl : modelApiUrl;

            System.out.println("=== AI模型调用调试信息 ===");
            System.out.println("1. 最终使用的API地址: " + apiUrl);
            System.out.println("2. 消息内容: " + message);
            System.out.println("3. 默认API地址配置: " + modelApiUrl);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "qwen-model");
            requestBody.put("messages", List.of(
                    Map.of("role", "user", "content", message)
            ));
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1024);
            requestBody.put("stop", List.of("<think>", "<|im_end|>"));
            requestBody.put("stream", false);

            String url = apiUrl + "/v1/chat/completions";
            System.out.println("4. 完整请求URL: " + url);
            System.out.println("5. 请求体内容: " + requestBody);

            System.out.println("6. 开始发送请求到模型服务...");

            // 添加超时和错误处理
            Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);

            System.out.println("7. 收到模型响应: " + response);

            if (response == null) {
                System.out.println("8. 错误: 响应为null");
                return "抱歉，AI服务返回空响应";
            }

            System.out.println("9. 响应包含的键: " + response.keySet());

            // 解析响应
            if (response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                System.out.println("10. choices数量: " + choices.size());

                if (!choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    System.out.println("11. 第一个choice内容: " + firstChoice);

                    if (firstChoice.containsKey("message")) {
                        Map<String, Object> messageObj = (Map<String, Object>) firstChoice.get("message");
                        System.out.println("12. message对象: " + messageObj);

                        String content = (String) messageObj.get("content");
                        System.out.println("13. 最终返回内容: " + content);
                        System.out.println("=== 调用成功 ===");
                        return content;
                    } else {
                        System.out.println("14. 错误: choice中没有message字段");
                    }
                } else {
                    System.out.println("15. 错误: choices列表为空");
                }
            } else {
                System.out.println("16. 错误: 响应中没有choices字段");
                // 检查是否有错误信息
                if (response.containsKey("error")) {
                    System.out.println("17. 错误详情: " + response.get("error"));
                }
            }

            System.out.println("=== 调用失败 ===");
            return "抱歉，AI没有返回有效响应";

        } catch (Exception e) {
            System.err.println("!!! 调用AI模型时发生异常 !!!");
            System.err.println("异常类型: " + e.getClass().getName());
            System.err.println("异常信息: " + e.getMessage());
            e.printStackTrace();
            return "抱歉，调用AI服务时发生错误: " + e.getMessage();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Boolean>> healthCheck(@RequestParam(required = false) String apiUrl) {
        try {
            String checkUrl = (apiUrl != null && !apiUrl.trim().isEmpty() ? apiUrl : modelApiUrl) + "/health";
            restTemplate.getForEntity(checkUrl, String.class);
            return ResponseEntity.ok(ApiResponse.success(true));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.success(false));
        }
    }
}