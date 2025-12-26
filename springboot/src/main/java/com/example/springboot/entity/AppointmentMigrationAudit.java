package com.example.springboot.entity;

import lombok.Data;
import java.util.Date;

@Data
public class AppointmentMigrationAudit {
    private Long id;
    private Long appointmentId;
    private Long fromScheduleId;
    private Long toScheduleId;
    private Long operatorId;
    private String note;
    private Date createdAt;
}


