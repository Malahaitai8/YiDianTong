package com.example.springboot.entity;

import lombok.Data;

@Data
public class Patient {
    private Long id;
    private Long userId;
    private String name;
    private String specificRole;
    private String idStatus;
    private String phoneNumber;
    private String idCardNumber;
    private User user;
}

