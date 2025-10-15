package com.example.springboot.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String role;
    private String status;
    private LocalDateTime createdAt;
}
