package com.example.springboot.service;

import com.example.springboot.dto.ScheduleDTO;
import com.example.springboot.entity.Schedule;
import com.example.springboot.mapper.ScheduleMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class ScheduleService {

    @Resource
    private ScheduleMapper scheduleMapper;

    private final String[] DAYS_OF_WEEK = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};

    public List<ScheduleDTO> getWeekSchedule() {
        // 获取当前日期
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startDate = calendar.getTime();

        // 获取一周后的日期
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        Date endDate = calendar.getTime();

        // 查询数据库
        List<Schedule> schedules = scheduleMapper.selectSchedulesByDateRange(startDate, endDate);
        
        // 转换为DTO并添加星期几信息
        List<ScheduleDTO> scheduleDTOs = new ArrayList<>();
        for (Schedule schedule : schedules) {
            ScheduleDTO dto = new ScheduleDTO();
            BeanUtils.copyProperties(schedule, dto);
            
            // 获取星期几
            calendar.setTime(schedule.getScheduleDate());
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            dto.setDayOfWeek(DAYS_OF_WEEK[dayOfWeek]);
            
            scheduleDTOs.add(dto);
        }

        return scheduleDTOs;
    }
}