package com.example.springboot.dto;

import lombok.Data;

@Data
public class QaMatchedFaqDTO {
    private String question;
    private String answer;
    private double score;
}


