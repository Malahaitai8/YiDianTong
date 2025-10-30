package com.example.springboot.dto;

import lombok.Data;
import java.util.List;

@Data
public class BatchDepartmentRequest {
    private List<DepartmentItem> departments;

    @Data
    public static class DepartmentItem {
        private String name;
        private String description;
    }
}

