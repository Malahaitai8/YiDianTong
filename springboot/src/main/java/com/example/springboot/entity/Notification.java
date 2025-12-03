package com.example.springboot.entity;

import lombok.Data;

import java.util.Date;

/**
 * 消息通知实体
 */
@Data
public class Notification {
    private Long id;
    private Long userId;
    private Long patientId;
    private String notificationType;
    private String title;
    private String content;
    private String relatedType;
    private Long relatedId;
    private String channel;
    private String status;
    private Date sendTime;
    private Date readTime;
    private Integer isRead;
    private String errorMessage;
    private String templateId;
    private String templateData;
    private Date createdAt;
    private Date updatedAt;
}
































