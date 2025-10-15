package com.example.springboot.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Waitlist {
    private Long id;
    private Long scheduleId;
    private Long patientId;
    private Date joinTime;
    private String status;
}

