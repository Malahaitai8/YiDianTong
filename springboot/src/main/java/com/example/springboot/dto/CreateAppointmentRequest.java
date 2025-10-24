package com.example.springboot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Date;

/**
 * 创建预约请求 DTO
 * 对应 API文档 7.5 节
 */
@Data
public class CreateAppointmentRequest {

    @NotNull(message = "scheduleId不能为空")
    private Long scheduleId;

    /**
     * 预约时间
     * 格式: yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @NotNull(message = "appointmentTime不能为空")
    private Date appointmentTime;
}