package com.example.springboot.dto;

import lombok.Data;

import java.util.Map;

@Data
public class QaAskRequest {
    private String query;
    private Map<String, Object> context;
}


