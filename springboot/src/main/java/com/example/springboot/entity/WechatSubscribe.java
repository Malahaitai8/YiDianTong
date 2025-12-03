package com.example.springboot.entity;

import lombok.Data;

import java.util.Date;

/**
 * 微信订阅授权记录
 */
@Data
public class WechatSubscribe {
    private Long id;
    private Long userId;
    private Long patientId;
    private String openid;
    private String templateId;
    private String templateName;
    private Boolean authorized;
    private Date authorizedAt;
    private Date expiresAt;
    private Date createdAt;
    private Date updatedAt;
}








