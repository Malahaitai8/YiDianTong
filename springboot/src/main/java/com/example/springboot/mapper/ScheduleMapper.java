package com.example.springboot.mapper;

import com.example.springboot.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

@Mapper
public interface ScheduleMapper {
    @Select("SELECT * FROM schedule WHERE schedule_date >= #{startDate} AND schedule_date < #{endDate}")
    List<Schedule> selectSchedulesByDateRange(Date startDate, Date endDate);
}