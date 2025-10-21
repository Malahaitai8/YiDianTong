package com.example.springboot.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.example.springboot.entity.Schedule;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleDTO extends Schedule {
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private String dayOfWeek;
}
