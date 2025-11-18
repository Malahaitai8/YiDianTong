package com.example.springboot.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 加号请求DTO
 */
@Data
public class AddSlotsRequest {
    
    /**
     * 要增加的号源数量
     */
    @NotNull(message = "增加的号源数量不能为空")
    @Min(value = 1, message = "增加的号源数量至少为1")
    @Max(value = 50, message = "单次增加的号源数量不能超过50")
    private Integer slotsToAdd;
    
    /**
     * 加号原因（可选）
     */
    private String reason;
}
