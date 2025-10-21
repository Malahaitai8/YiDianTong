package com.example.springboot.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class Appointment {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private Long scheduleId;
    private Date appointmentTime;
    private String status;
    private BigDecimal fee;
    private BigDecimal actualFee;
    private Date createdAt;
    private String sourceType;
}

