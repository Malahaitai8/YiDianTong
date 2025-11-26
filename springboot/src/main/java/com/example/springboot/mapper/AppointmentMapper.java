package com.example.springboot.mapper;

import com.example.springboot.entity.Appointment;
import com.example.springboot.dto.AppointmentWithDoctorDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AppointmentMapper {
    List<Appointment> selectAll();

    Appointment selectById(Long id);

    /** 新增预约 */
    int insert(Appointment appointment);

    /** 根据主键删除预约 */
    int deleteById(Long id);

    /** 查询当前患者的预约列表 */
    List<Appointment> selectByPatientId(Long patientId);

    /** 查询当前患者的预约列表（包含医生信息） */
    List<AppointmentWithDoctorDTO> selectByPatientIdWithDoctorInfo(Long patientId);

    /** 查询指定医生的预约列表 */
    List<Appointment> selectByDoctorId(Long doctorId);

    /** 统计某排班的预约数量 */
    int countByScheduleId(Long scheduleId);

    /** 更新预约状态 */
    int updateStatus(Long id, String status);

    /** 根据主键更新预约信息 */
    int updateById(Appointment appointment);

    /** 统计患者在某天的预约数量（不含已取消） */
    int countByPatientAndDate(Long patientId, java.util.Date dayStart, java.util.Date dayEnd);

    /** 判断是否已对同一排班预约（不含已取消） */
    int existsByPatientAndSchedule(Long patientId, Long scheduleId);

    // ========== 统计接口方法 ==========

    /**
     * 根据状态统计预约数量
     */
    int countByStatus(@Param("status") String status);

    /**
     * 统计总预约数量
     */
    int countTotal();
    
    /**
     * 根据时间范围查询预约（用于就诊提醒）
     */
    List<Appointment> selectByTimeRange(@Param("startTime") java.util.Date startTime,
                                        @Param("endTime") java.util.Date endTime);
}