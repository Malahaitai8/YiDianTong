package com.example.springboot.dto;

import lombok.Data;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 排班查询请求DTO
 */
@Data
public class ScheduleQueryRequest {
    
    private Long doctorId;
    
    private Long departmentId;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;
    
    private String timeSlot; // morning/afternoon/evening
    
    private String slotType; // normal/expert/vip
    
    // 分页参数
    private Integer page = 1;
    
    private Integer pageSize = 20;
}

