package com.example.springboot.mapper;

import com.example.springboot.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param; // <-- [新增] 导入
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update; // <-- [新增] 导入

import java.util.Date;
import java.util.List;
import com.example.springboot.dto.AvailableSlotDTO;

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

}