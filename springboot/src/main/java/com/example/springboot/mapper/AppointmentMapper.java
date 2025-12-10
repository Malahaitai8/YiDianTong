package com.example.springboot.mapper;

import com.example.springboot.entity.Appointment;
import com.example.springboot.dto.AppointmentWithDoctorDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
    int countByStatus(@Param("status") String status, 
                     @Param("departmentId") Long departmentId,
                     @Param("startDate") Date startDate,
                     @Param("endDate") Date endDate);

    /**
     * 统计总预约数量
     */
    int countTotal(@Param("departmentId") Long departmentId,
                   @Param("startDate") Date startDate,
                   @Param("endDate") Date endDate);

    // ---------- 统计报表用 ----------

    List<Map<String, Object>> countByDateRange(@Param("startDate") Date startDate,
                                               @Param("endDate") Date endDate);

    List<Map<String, Object>> countByDepartment(@Param("startDate") Date startDate,
                                                @Param("endDate") Date endDate);

    List<Map<String, Object>> countByDoctor(@Param("startDate") Date startDate,
                                            @Param("endDate") Date endDate,
                                            @Param("doctorId") Long doctorId);

    List<Map<String, Object>> sumRevenueByDateRange(@Param("startDate") Date startDate,
                                                    @Param("endDate") Date endDate,
                                                    @Param("departmentId") Long departmentId);

    List<Map<String, Object>> sumRevenueByDepartment(@Param("startDate") Date startDate,
                                                     @Param("endDate") Date endDate);

    List<Map<String, Object>> countBySlotType(@Param("startDate") Date startDate,
                                              @Param("endDate") Date endDate,
                                              @Param("departmentId") Long departmentId);

    List<Map<String, Object>> countByTimeSlot(@Param("startDate") Date startDate,
                                              @Param("endDate") Date endDate,
                                              @Param("departmentId") Long departmentId);

    List<Map<String, Object>> getCancellationRateByDepartment(@Param("startDate") Date startDate,
                                                              @Param("endDate") Date endDate);

    List<Map<String, Object>> getCancellationRateByDoctor(@Param("startDate") Date startDate,
                                                          @Param("endDate") Date endDate,
                                                          @Param("doctorId") Long doctorId);

    List<Map<String, Object>> getAppointmentTrend(@Param("startDate") Date startDate,
                                                  @Param("endDate") Date endDate,
                                                  @Param("departmentId") Long departmentId);

    /**
     * 获取预约原始明细列表（用于Excel导出）
     */
    List<Map<String, Object>> getAppointmentRawDetails(@Param("startDate") Date startDate,
                                                       @Param("endDate") Date endDate,
                                                       @Param("departmentId") Long departmentId);
}