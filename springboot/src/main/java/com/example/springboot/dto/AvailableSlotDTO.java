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
    
    /** 医生ID */
    private Long doctorId;

    /** 医生姓名 */
    private String doctorName;

    /** 医生职称 */
    private String doctorTitle;

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

    /** 号别类型：NORMAL/EXPERT/VIP（用于在 Service 中按 system_config 计算费用） */
    private String slotType;

    /** 挂号费（由 Service 根据 slotType 和 system_config 计算填充） */
    private Double fee;
    
    /** 医生级别映射值（由后端填充，值越大表示职称越高） */
    private Integer doctorRank;
    
    public Integer getDoctorRank() {
        return doctorRank;
    }

    public void setDoctorRank(Integer doctorRank) {
        this.doctorRank = doctorRank;
    }

    // 手动添加可能缺失的getter和setter方法
    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getSlotType() {
        return slotType;
    }

    public void setSlotType(String slotType) {
        this.slotType = slotType;
    }

    public Double getFee() {
        return fee;
    }

    public void setFee(Double fee) {
        this.fee = fee;
    }
}
