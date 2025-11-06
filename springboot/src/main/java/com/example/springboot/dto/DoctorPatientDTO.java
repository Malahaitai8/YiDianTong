package com.example.springboot.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 医生查看患者预约DTO
 */
@Data
public class DoctorPatientDTO {
    private Long appointmentId;
    private Long patientId;
    private String patientName;
    private String phoneNumber;
    private String idCardNumber;
    private String specificRole;
    private Date appointmentTime;
    private String status;
    private String statusName;
    private BigDecimal fee;
    private BigDecimal actualFee;
    private String sourceType;
    private Date createdAt;
    private Long scheduleId;
    private Date scheduleDate;
    private String timeSlot;
    private String timeSlotName;
}

