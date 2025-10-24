package com.example.springboot.dto;

import lombok.Data;

import java.util.Date;

/**
 * 排班查询请求DTO
 */
@Data
public class ScheduleQueryRequest {
    
    private Long doctorId;
    
    private Long departmentId;
    
    private Date startDate;
    
    private Date endDate;
    
    private String timeSlot; // morning/afternoon/evening
    
    private String slotType; // normal/expert/vip
    
    // 分页参数
    private Integer page = 1;
    
    private Integer pageSize = 20;
}

