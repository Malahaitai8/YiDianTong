package com.example.springboot.dto;

import lombok.Data;

@Data
public class DoctorChangeRequestItem {
    private String id; // request id
    private Long doctorId;
    private Long requestedByUserId;
    private String status; // PENDING/APPROVED/REJECTED
    private Long createdAt;
    private Long updatedAt;
    private Long clinicId;
    private String name;
    private String title;
    private String specialty;
    private String bio;
    private String approvedBy;
    private Long approvedAt;
    private String reason;
}


