package com.example.springboot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Date;

/**
 * 创建预约请求 DTO
 * 对应 API文档 7.4 节
 */
@Data
public class CreateAppointmentRequest {

    @NotNull(message = "scheduleId不能为空")
    private Long scheduleId;

    /**
     * 预约的精确时间
     * (对应 API文档 7.4 节的 appointmentDate 和 timeSlot 的组合)
     * 前端应根据排班信息计算出此值
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @NotNull(message = "appointmentTime不能为空")
    @Future(message = "appointmentTime必须是未来时间")
    private Date appointmentTime;
}