package com.example.springboot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 可预约时段 DTO
 * 对应 API文档 7.4 节搜索结果
 */
@Data
public class AvailableSlotDTO {
    
    /** 排班ID */
    private Long scheduleId;
    
    /** 医生姓名 */
    private String doctorName;
    
    /** 科室名称 */
    private String departmentName;
    
    /** 日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date date;
    
    /** 时间段 */
    private String timeSlot;
    
    /** 开始时间 */
    private String startTime;
    
    /** 结束时间 */
    private String endTime;
    
    /** 可用号源数 */
    private Integer availableSlots;
    
    /** 总号源数 */
    private Integer totalSlots;
    
    /** 挂号费 */
    private Double fee;
    
    /** 优惠后费用 */
    private Double discountedFee;
}
