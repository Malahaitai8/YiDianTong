package com.example.springboot.dto;

import com.example.springboot.entity.Schedule;
import com.fasterxml.jackson.annotation.JsonFormat;

public class ScheduleDTO extends Schedule {
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private String dayOfWeek;

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
}
