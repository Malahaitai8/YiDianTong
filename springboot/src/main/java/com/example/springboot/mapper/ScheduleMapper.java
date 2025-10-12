package com.example.springboot.mapper;

import com.example.springboot.entity.Schedule;

import java.util.List;

public interface ScheduleMapper {
    List<Schedule> selectAll();

    Schedule selectById(Long id);
}

