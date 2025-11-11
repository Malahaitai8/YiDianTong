package com.example.springboot.mapper;

import com.example.springboot.entity.Waitlist;

import java.util.List;

public interface WaitlistMapper {
    List<Waitlist> selectAll();

    Waitlist selectById(Long id);

    int insert(Waitlist waitlist);

    int update(Waitlist waitlist);

    int delete(Long id);

    /** 根据患者 ID 查询其候补队列 */
    List<Waitlist> selectByPatientId(Long patientId);

    /** 查询指定排班下最早的 WAITING 状态记录 */
    Waitlist selectNextWaiting(@org.apache.ibatis.annotations.Param("scheduleId") Long scheduleId);

    /** 更新候补记录状态 */
    int updateStatus(@org.apache.ibatis.annotations.Param("id") Long id, @org.apache.ibatis.annotations.Param("status") String status);

    /** 判断是否已存在相同患者对同一排班的候补记录 */
    int existsByPatientAndSchedule(@org.apache.ibatis.annotations.Param("patientId") Long patientId,
                                   @org.apache.ibatis.annotations.Param("scheduleId") Long scheduleId);
}

