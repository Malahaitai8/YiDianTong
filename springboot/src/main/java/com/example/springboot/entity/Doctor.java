package com.example.springboot.entity;

import lombok.Data;

@Data
public class Doctor {
    private Long id;
    private Long userId;
    private Long clinicId;
    private String name;
    private String title;
    private String specialty;
    private String bio;
    private User user;
    private Clinic clinic;
    private Boolean onDutyToday;
}

