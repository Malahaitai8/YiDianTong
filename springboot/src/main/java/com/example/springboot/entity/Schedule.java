package com.example.springboot.entity;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

@Data
public class Schedule {
    private Long id;
    private Long doctorId;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date scheduleDate;
    private String timeSlot;
    private String slotType;
    private Integer totalSlots;
    private Integer availableSlots;
}

