package com.example.springboot.mapper;

import com.example.springboot.entity.Waitlist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WaitlistMapper {
    List<Waitlist> selectAll();

    Waitlist selectById(Long id);

    Waitlist selectByScheduleAndPatient(@Param("scheduleId") Long scheduleId,
                                        @Param("patientId") Long patientId);

    int insert(Waitlist waitlist);

    int update(Waitlist waitlist);

    int delete(Long id);

    /** 根据患者 ID 查询其候补队列 */
    List<Waitlist> selectByPatientId(Long patientId);

    /** 查询指定排班下最早的 WAITING 状态记录 */
    Waitlist selectNextWaiting(@org.apache.ibatis.annotations.Param("scheduleId") Long scheduleId);

    /** 更新候补记录状态 */
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /** 判断是否已存在相同患者对同一排班的候补记录 */
    int existsByPatientAndSchedule(@Param("patientId") Long patientId,
                                   @Param("scheduleId") Long scheduleId);

    /**
     * 统计同类时段的历史候补成功率
     * @param doctorId 医生ID
     * @param timeSlot 时间段 (morning/afternoon/evening)
     * @param days 统计最近多少天的数据（默认7天）
     * @return 成功率（0-100之间的数值）
     */
    Double calculateSuccessRate(@Param("doctorId") Long doctorId,
                               @Param("timeSlot") String timeSlot,
                               @Param("days") Integer days);

    /**
     * 计算历史同类候补的平均等待时长（小时）
     * @param doctorId 医生ID
     * @param timeSlot 时间段 (morning/afternoon/evening)
     * @param days 统计最近多少天的数据（默认7天）
     * @return 平均等待时长（小时）
     */
    Double calculateAvgWaitTime(@Param("doctorId") Long doctorId,
                                @Param("timeSlot") String timeSlot,
                                @Param("days") Integer days);
}

