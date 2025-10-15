package com.example.springboot.entity;

import lombok.Data;

@Data
public class Admin {
    private Long id;
    private Long userId;
    private String name;
    private String adminRole;
    private User user;
}

