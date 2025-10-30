package com.example.springboot.dto;

import lombok.Data;
import java.util.List;

@Data
public class BatchClinicRequest {
    private Long departmentId; // 选定的科室ID
    private List<ClinicItem> clinics;

    @Data
    public static class ClinicItem {
        private String name;
        private String description;
    }
}

