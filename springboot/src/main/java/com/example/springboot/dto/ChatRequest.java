package com.example.springboot.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String message;
    private String apiUrl;
    private Double temperature = 0.7;
    private Integer maxTokens = 1024;
}