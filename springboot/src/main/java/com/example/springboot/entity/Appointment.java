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
    private Boolean autoAssigned; // 系统自动分配标记
    private Date rescheduleWindowExpires; // 自动分配的患者可重新选择窗口截止时间
    private Date updatedAt; // 更新时间
}

