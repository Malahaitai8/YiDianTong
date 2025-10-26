package com.example.springboot.dto;

import lombok.Data;

@Data
public class DoctorChangeRequestDTO {
    private Long doctorId;
    private Long requestedByUserId;
    private Long clinicId;
    private String clinicName;
    private String name;
    private String title;
    private String specialty;
    private String bio;
}


