package com.example.springboot.dto;

/**
 * 登录响应DTO
 */
public class LoginResponse {
    
    private String token;
    private Long userId;
    private String username;
    private String role;
    private String status;

    public LoginResponse() {
    }

    public LoginResponse(String token, Long userId, String username, String role, String status) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

