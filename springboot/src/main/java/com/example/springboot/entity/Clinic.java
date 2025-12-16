package com.example.springboot.entity;

import lombok.Data;

@Data
public class Clinic {
    private Long id;
    private Long departmentId;
    private String name;
    
    private String description;
    private Department department;
}

