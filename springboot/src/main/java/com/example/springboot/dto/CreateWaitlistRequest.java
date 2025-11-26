package com.example.springboot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 加入候补队列请求 DTO
 * 对应 API文档 8.1 节
 */
@Data
public class CreateWaitlistRequest {
    @NotNull(message = "scheduleId不能为空")
    private Long scheduleId;

    @NotNull(message = "waitlistId不能为空")
    private Long waitlistId;
}