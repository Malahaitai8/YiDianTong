package com.example.springboot.controller;

import com.example.springboot.service.DeepSeekService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deepseek")
@Slf4j
public class DeepSeekController {

    private final DeepSeekService deepSeekService;

    public DeepSeekController(DeepSeekService deepSeekService) {
        this.deepSeekService = deepSeekService;
    }

    // 使用 Map 接收请求体
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> request) {
        try {
            String userMessage = request.get("message");
            String response = deepSeekService.chat(userMessage);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", Map.of("message", response),
                    "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("聊天处理失败: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    // 简单版本 - 表单参数
    @PostMapping("/simple-chat")
    public Map<String, Object> simpleChat(@RequestParam String message) {
        try {
            String response = deepSeekService.chat(message);
            return Map.of("status", "success", "response", response);
        } catch (Exception e) {
            return Map.of("status", "error", "message", e.getMessage());
        }
    }

    // 多轮对话接口
    @PostMapping("/chat/history")
    public ResponseEntity<Map<String, Object>> chatWithHistory(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> messageHistory = (List<Map<String, String>>) request.get("messages");
            String response = deepSeekService.chatWithHistory(messageHistory);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", Map.of("message", response)
            ));

        } catch (Exception e) {
            log.error("多轮对话处理失败: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
}