package com.example.springboot.dto;

import lombok.Data;
import java.util.Date;

/**
 * 医生排班DTO
 */
@Data
public class DoctorScheduleDTO {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private Date scheduleDate;
    private String timeSlot;
    private String timeSlotName;
    private String slotType;
    private String slotTypeName;
    private Integer totalSlots;
    private Integer availableSlots;
    private Integer bookedSlots;
    private String dayOfWeek;
}

