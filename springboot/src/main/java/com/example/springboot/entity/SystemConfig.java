package com.example.springboot.entity;

import lombok.Data;
import java.util.Date;

@Data
public class SystemConfig {
    private Long id;
    private String key;
    private String value;
    private String description;
    private Date createdAt;
    private Date updatedAt;
}

