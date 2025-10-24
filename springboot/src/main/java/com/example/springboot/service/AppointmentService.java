package com.example.springboot.service;

import com.example.springboot.entity.Appointment;
import com.example.springboot.entity.Waitlist;
import com.example.springboot.mapper.AppointmentMapper;
import com.example.springboot.mapper.ScheduleMapper;
import com.example.springboot.dto.AvailableSlotDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Date;

@Service
public class AppointmentService {

    @Resource
    private AppointmentMapper appointmentMapper;

    @Resource
    private WaitlistService waitlistService;

    @Resource
    private ScheduleMapper scheduleMapper;

    public List<Appointment> selectAll() {

        List<Appointment> list = appointmentMapper.selectAll();

        return list;
    }

    /** 新增预约 */
    public Appointment create(Appointment appointment) {
        appointmentMapper.insert(appointment);
        return appointment;
    }

    /** 根据主键删除 */
    public int deleteById(Long id) {
        // 先查询预约信息，获取 scheduleId
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            return 0;
        }
        
        // 删除预约
        int result = appointmentMapper.deleteById(id);
        
        // 如果删除成功，尝试从候补队列中弹出下一个患者并创建预约
        if (result > 0) {
            processWaitlistAfterDeletion(appointment.getScheduleId());
        }
        
        return result;
    }

    /** 处理候补队列：当预约被删除后，为队首患者创建预约 */
    private void processWaitlistAfterDeletion(Long scheduleId) {
        try {
            // 从候补队列中弹出下一个患者
            Waitlist nextWaitlist = waitlistService.popNext(scheduleId);
            if (nextWaitlist != null) {
                // 为队首患者创建预约
                Appointment newAppointment = new Appointment();
                newAppointment.setPatientId(nextWaitlist.getPatientId());
                newAppointment.setScheduleId(scheduleId);
                newAppointment.setStatus("PENDING");
                newAppointment.setSourceType("WAITLIST");
                newAppointment.setCreatedAt(new java.util.Date());
                
                // 这里需要获取医生ID，暂时设为null，实际应该从schedule表查询
                // newAppointment.setDoctorId(doctorId);
                
                appointmentMapper.insert(newAppointment);
            }
        } catch (Exception e) {
            // 记录日志但不影响主流程
            System.err.println("处理候补队列失败: " + e.getMessage());
        }
    }

    /** 取消预约（更新状态为CANCELLED） */
    public int cancelById(Long id) {
        return appointmentMapper.updateStatus(id, "CANCELLED");
    }

    /** 根据患者 ID 查询预约列表 */
    public List<Appointment> listByPatient(Long patientId) {
        return appointmentMapper.selectByPatientId(patientId);
    }

    public Appointment selectById(Long id) {
        Appointment appointment = appointmentMapper.selectById(id);
        return appointment;
    }

    /** 更新预约信息 */
    public Appointment update(Appointment appointment) {
        appointmentMapper.updateById(appointment);
        return appointment;
    }

    /** 搜索可预约时段 */
    public List<AvailableSlotDTO> searchAvailableSlots(
            Long departmentId, 
            Long doctorId, 
            Date startDate, 
            Date endDate, 
            String timeSlot) {
        return scheduleMapper.searchAvailableSlots(departmentId, doctorId, startDate, endDate, timeSlot);
    }
}

