package com.example.springboot.service;

import com.example.springboot.config.SecurityUtils;
import com.example.springboot.entity.Appointment;
import com.example.springboot.entity.Patient;
import com.example.springboot.entity.Waitlist;
import com.example.springboot.exception.CustomerException;
import com.example.springboot.mapper.AppointmentMapper;
import com.example.springboot.mapper.PatientMapper;
import com.example.springboot.mapper.ScheduleMapper;
import com.example.springboot.dto.AvailableSlotDTO;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Date;

@Service
public class AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);

    @Resource
    private AppointmentMapper appointmentMapper;

    @Resource
    private PatientMapper patientMapper;

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

    /**
     * 根据主键删除预约
     * 权限检查：管理员可以删除任何预约，患者只能删除自己的预约
     */
    public int deleteById(Long id) {
        // 先查询预约信息，获取 scheduleId
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new CustomerException("预约不存在");
        }
        
        // 如果当前用户不是管理员，则检查是否为本人操作
        if (!SecurityUtils.isAdmin()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (currentUserId == null) {
                throw new CustomerException("401", "未登录");
            }
            
            // 通过 patientId 查询患者信息，获取对应的 userId
            Patient patient = patientMapper.selectById(appointment.getPatientId());
            if (patient == null) {
                throw new CustomerException("患者信息不存在");
            }
            
            // 检查是否为本人（比较 userId）
            if (!currentUserId.equals(patient.getUserId())) {
                logger.warn("用户 {} 尝试删除预约 {}，但该预约属于用户 {}", 
                           currentUserId, id, patient.getUserId());
                throw new CustomerException("403", "无权限删除其他患者的预约");
            }
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
                
                // 从 schedule 表查询医生ID
                var schedule = scheduleMapper.selectById(scheduleId);
                if (schedule != null) {
                    newAppointment.setDoctorId(schedule.getDoctorId());
                }
                
                appointmentMapper.insert(newAppointment);
                logger.info("候补队列自动创建预约成功: 患者ID={}, 排班ID={}", 
                           nextWaitlist.getPatientId(), scheduleId);
            }
        } catch (Exception e) {
            // 记录日志但不影响主流程
            logger.error("处理候补队列失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 取消预约（更新状态为CANCELLED）
     * 权限检查：管理员可以取消任何预约，患者只能取消自己的预约
     */
    public int cancelById(Long id) {
        // 查询预约信息
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new CustomerException("预约不存在");
        }
        
        // 如果当前用户不是管理员，则检查是否为本人操作
        if (!SecurityUtils.isAdmin()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (currentUserId == null) {
                throw new CustomerException("401", "未登录");
            }
            
            // 通过 patientId 查询患者信息，获取对应的 userId
            Patient patient = patientMapper.selectById(appointment.getPatientId());
            if (patient == null) {
                throw new CustomerException("患者信息不存在");
            }
            
            // 检查是否为本人（比较 userId）
            if (!currentUserId.equals(patient.getUserId())) {
                logger.warn("用户 {} 尝试取消预约 {}，但该预约属于用户 {}", 
                           currentUserId, id, patient.getUserId());
                throw new CustomerException("403", "无权限取消其他患者的预约");
            }
        }
        
        int result = appointmentMapper.updateStatus(id, "CANCELLED");
        
        // 如果取消成功，尝试从候补队列中弹出下一个患者并创建预约
        if (result > 0) {
            processWaitlistAfterDeletion(appointment.getScheduleId());
        }
        
        return result;
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

