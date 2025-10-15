package com.example.springboot.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String newPassword;
}
