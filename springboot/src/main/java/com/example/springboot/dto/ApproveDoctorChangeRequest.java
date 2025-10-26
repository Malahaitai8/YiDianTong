package com.example.springboot.dto;

import lombok.Data;

@Data
public class ApproveDoctorChangeRequest {
    private String id; // request id
    private String action; // APPROVE or REJECT
    private String reason; // optional when reject
}


