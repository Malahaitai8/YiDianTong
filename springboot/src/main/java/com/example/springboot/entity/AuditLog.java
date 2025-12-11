package com.example.springboot.entity;

import lombok.Data;

import java.util.Date;

/**
 * 审计日志实体
 */
@Data
public class AuditLog {
    private Long id;
    private Long userId;
    private String username;
    private String userRole;
    private String operationType;
    private String operationModule;
    private String operationDesc;
    private String targetType;
    private Long targetId;
    private String requestMethod;
    private String requestUrl;
    private String requestParams;
    private String responseCode;
    private String responseMsg;
    private String ipAddress;
    private String userAgent;
    private Long executionTime;
    private String status;
    private String errorMessage;
    private Date createdAt;
}































