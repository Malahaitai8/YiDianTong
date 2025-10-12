package com.example.springboot.service;

import com.example.springboot.entity.Schedule;
import com.example.springboot.mapper.ScheduleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleService {

    @Resource
    private ScheduleMapper scheduleMapper;

    public List<Schedule> selectAll() {

        List<Schedule> list = scheduleMapper.selectAll();

        return list;
    }

    public Schedule selectById(Long id) {
        Schedule schedule = scheduleMapper.selectById(id);
        return schedule;
    }
}

