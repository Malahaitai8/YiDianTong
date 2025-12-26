package com.example.springboot.dto;

import com.example.springboot.entity.Appointment;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class AppointmentWithDoctorDTO {
    // Appointment 字段
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
    
    // Doctor 信息
    private String doctorName;
    private String doctorTitle;
    
    // Schedule 信息
    private Date scheduleDate;
    private String timeSlot;
    private String slotType;
    
    // 自动分配标记与重新选择窗口
    private Boolean autoAssigned;
    private Date rescheduleWindowExpires;
    
    // Department/Clinic 信息
    private String departmentName;
    private String clinicName;
    
    public static AppointmentWithDoctorDTO from(Appointment appointment, String doctorName, String doctorTitle, 
                                               Date scheduleDate, String timeSlot, String slotType) {
        AppointmentWithDoctorDTO dto = new AppointmentWithDoctorDTO();
        
        // 复制 Appointment 字段
        dto.setId(appointment.getId());
        dto.setPatientId(appointment.getPatientId());
        dto.setDoctorId(appointment.getDoctorId());
        dto.setScheduleId(appointment.getScheduleId());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setStatus(appointment.getStatus());
        dto.setFee(appointment.getFee());
        dto.setActualFee(appointment.getActualFee());
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setSourceType(appointment.getSourceType());
        dto.setAutoAssigned(appointment.getAutoAssigned());
        dto.setRescheduleWindowExpires(appointment.getRescheduleWindowExpires());
        
        // 设置医生信息
        dto.setDoctorName(doctorName);
        dto.setDoctorTitle(doctorTitle);
        
        // 设置排班信息
        dto.setScheduleDate(scheduleDate);
        dto.setTimeSlot(timeSlot);
        dto.setSlotType(slotType);
        
        return dto;
    }
}