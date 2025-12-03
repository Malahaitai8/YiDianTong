package com.example.springboot.dto;

import lombok.Data;

@Data
public class QaTopQuestionDTO {
    private String question;
    private Integer count;

    public QaTopQuestionDTO(String question, int count) {
        this.question = question;
        this.count = count;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}


