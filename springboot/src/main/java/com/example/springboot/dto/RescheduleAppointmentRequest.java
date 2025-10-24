package com.example.springboot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 改约请求 DTO
 * 对应 API文档 7.6 节
 */
@Data
public class RescheduleAppointmentRequest {

    @NotNull(message = "newScheduleId不能为空")
    private Long newScheduleId;

    /**
     * 改约原因
     */
    private String reason;
}
