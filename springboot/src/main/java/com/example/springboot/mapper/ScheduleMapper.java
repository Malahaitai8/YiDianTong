package com.example.springboot.mapper;

import com.example.springboot.entity.Schedule;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;
import com.example.springboot.dto.AvailableSlotDTO;
import com.example.springboot.dto.ScheduleWithDetailsDTO;

@Mapper
public interface ScheduleMapper {
    @Select("SELECT * FROM schedule WHERE schedule_date >= #{startDate} AND schedule_date < #{endDate}")
    List<Schedule> selectSchedulesByDateRange(Date startDate, Date endDate);

    /**
     * 根据ID查询排班
     * (实现在 ScheduleMapper.xml 中)
     */
    Schedule selectById(Long id);

    /**
     * 根据ID查询排班详细信息（包含医生、科室等信息）
     * (实现在 ScheduleMapper.xml 中)
     */
    ScheduleWithDetailsDTO selectByIdWithDetails(@Param("id") Long id);

    /**
     * 扣减号源 (乐观锁)
     * @return 影响行数，0表示扣减失败（号源不足）
     */
    @Update("UPDATE schedule SET available_slots = available_slots - 1 WHERE id = #{id} AND available_slots > 0")
    int decreaseAvailableSlots(@Param("id") Long id);

    /**
     * 归还号源 (取消或删除时)
     */
    @Update("UPDATE schedule SET available_slots = available_slots + 1 WHERE id = #{id}")
    int increaseAvailableSlots(@Param("id") Long id);

    /**
     * 搜索可预约时段
     * @param departmentId 科室ID (可选)
     * @param doctorId 医生ID (可选)
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param timeSlot 时间段 (可选)
     * @return 可预约时段列表
     */
    List<AvailableSlotDTO> searchAvailableSlots(
        @Param("departmentId") Long departmentId,
        @Param("doctorId") Long doctorId,
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate,
        @Param("timeSlot") String timeSlot
    );

    // ========== 管理端排班管理接口 ==========

    /**
     * 插入排班记录
     */
    @Insert("INSERT INTO schedule (doctor_id, schedule_date, time_slot, slot_type, total_slots, available_slots) " +
            "VALUES (#{doctorId}, #{scheduleDate}, #{timeSlot}, #{slotType}, #{totalSlots}, #{availableSlots})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Schedule schedule);

    /**
     * 更新排班记录
     * (实现在 ScheduleMapper.xml 中，支持动态更新)
     */
    int updateById(Schedule schedule);

    /**
     * 删除排班记录
     */
    @Delete("DELETE FROM schedule WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 根据医生ID查询排班列表
     */
    @Select("SELECT * FROM schedule WHERE doctor_id = #{doctorId} ORDER BY schedule_date, time_slot")
    List<Schedule> selectByDoctorId(@Param("doctorId") Long doctorId);

    /**
     * 根据医生ID查询排班详细信息（包含医生姓名、门诊信息等）
     * (实现在 ScheduleMapper.xml 中)
     */
    List<ScheduleWithDetailsDTO> selectByDoctorIdWithDetails(@Param("doctorId") Long doctorId);

    /**
     * 根据医生ID和日期范围查询排班列表
     */
    @Select("SELECT * FROM schedule WHERE doctor_id = #{doctorId} " +
            "AND schedule_date >= #{startDate} AND schedule_date < #{endDate} " +
            "ORDER BY schedule_date, time_slot")
    List<Schedule> selectByDoctorAndDateRange(@Param("doctorId") Long doctorId,
                                               @Param("startDate") Date startDate,
                                               @Param("endDate") Date endDate);

    @Select("SELECT COUNT(*) FROM schedule WHERE doctor_id = #{doctorId} " +
            "AND schedule_date >= #{startDate} AND schedule_date < #{endDate}")
    int countByDoctorAndDateRange(@Param("doctorId") Long doctorId,
                                  @Param("startDate") Date startDate,
                                  @Param("endDate") Date endDate);

    /**
     * 检查排班是否已存在（防止重复创建）
     */
    @Select("SELECT COUNT(*) FROM schedule WHERE doctor_id = #{doctorId} " +
            "AND schedule_date = #{scheduleDate} AND time_slot = #{timeSlot}")
    int checkScheduleExists(@Param("doctorId") Long doctorId,
                           @Param("scheduleDate") Date scheduleDate,
                           @Param("timeSlot") String timeSlot);

    /**
     * 条件查询排班（管理端）
     * (实现在 ScheduleMapper.xml 中)
     */
    List<ScheduleWithDetailsDTO> selectByConditions(
        @Param("doctorId") Long doctorId,
        @Param("departmentId") Long departmentId,
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate,
        @Param("timeSlot") String timeSlot,
        @Param("slotType") String slotType
    );

    /**
     * 统计符合条件的排班数量
     */
    int countByConditions(
        @Param("doctorId") Long doctorId,
        @Param("departmentId") Long departmentId,
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate,
        @Param("timeSlot") String timeSlot,
        @Param("slotType") String slotType
    );

    /**
     * 删除指定医生、日期、时间段的排班（用于规则覆盖）
     */
    @Delete("DELETE FROM schedule WHERE doctor_id = #{doctorId} " +
            "AND schedule_date = #{scheduleDate} AND time_slot = #{timeSlot}")
    int deleteByDoctorDateTimeSlot(@Param("doctorId") Long doctorId,
                                    @Param("scheduleDate") Date scheduleDate,
                                    @Param("timeSlot") String timeSlot);

    // ========== 统计接口方法 ==========

    /**
     * 统计所有排班的总号源数
     */
    @Select("SELECT SUM(total_slots) FROM schedule")
    Integer sumTotalSlots();

    /**
     * 统计所有排班的总可用号源数
     */
    @Select("SELECT SUM(available_slots) FROM schedule")
    Integer sumAvailableSlots();
}
