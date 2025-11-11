package com.example.springboot.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FeesUpdateRequest {
    private BigDecimal normal;
    private BigDecimal expert;
    private BigDecimal vip;
}


