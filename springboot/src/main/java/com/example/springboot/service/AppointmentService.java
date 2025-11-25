package com.example.springboot.service;

import com.example.springboot.config.SecurityUtils;
import com.example.springboot.entity.Appointment;
import com.example.springboot.entity.Patient;
import com.example.springboot.entity.PrepaymentOrder;
// [删除] import com.example.springboot.entity.Waitlist; // 不再需要
import com.example.springboot.entity.Schedule; // <-- [新增] 导入
import com.example.springboot.exception.CustomerException;
import com.example.springboot.mapper.AppointmentMapper;
import com.example.springboot.mapper.PatientMapper;
import com.example.springboot.mapper.ScheduleMapper;
import com.example.springboot.dto.AvailableSlotDTO;
import com.example.springboot.dto.AppointmentWithDoctorDTO;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private PrepaymentOrderService prepaymentOrderService;

    public List<Appointment> selectAll() {

        List<Appointment> list = appointmentMapper.selectAll();

        return list;
    }

    /** 新增预约 */
    @Transactional
    public Appointment create(Appointment appointment) {
        // ========== 规则校验 ==========
        var schedule = scheduleMapper.selectById(appointment.getScheduleId());
        if (schedule == null) {
            throw new CustomerException("排班不存在");
        }

        // 预约时间必须为未来时间，且与排班日期一致
        Date now = new Date();
        if (appointment.getAppointmentTime() == null || !appointment.getAppointmentTime().after(now)) {
            throw new CustomerException("预约时间必须为未来时间");
        }
        Date dayStart = atStartOfDay(schedule.getScheduleDate());
        Date dayEnd = atStartOfNextDay(schedule.getScheduleDate());
        if (appointment.getAppointmentTime().before(dayStart) || !appointment.getAppointmentTime().before(dayEnd)) {
            throw new CustomerException("预约时间与排班日期不一致");
        }

        // 每个患者每天最多预约N次（默认3）
        Long patientId = appointment.getPatientId();
        int dailyLimit = systemConfigService.getIntOrDefault("APPOINTMENT_DAILY_LIMIT", 3);
        int todayCount = appointmentMapper.countByPatientAndDate(patientId, dayStart, dayEnd);
        if (todayCount >= dailyLimit) {
            throw new CustomerException("当天预约次数已达上限");
        }

        // 同一排班禁止重复预约
        int exists = appointmentMapper.existsByPatientAndSchedule(patientId, appointment.getScheduleId());
        if (exists > 0) {
            throw new CustomerException("请勿重复预约该排班");
        }

        // ========== 扣减号源（原子性） ==========
        int decreased = scheduleMapper.decreaseAvailableSlots(schedule.getId());
        if (decreased <= 0) {
            throw new CustomerException("号源不足");
        }

        // ========== 费用计算 ==========
        try {
            if (appointment.getFee() == null || appointment.getFee().compareTo(BigDecimal.ZERO) <= 0) {
                String slotType = schedule.getSlotType();
                String normalized = slotType == null ? "NORMAL" : slotType.trim().toUpperCase();
                String key;
                switch (normalized) {
                    case "EXPERT":
                        key = "FEE_EXPERT";
                        break;
                    case "VIP":
                        key = "FEE_VIP";
                        break;
                    default:
                        key = "FEE_NORMAL";
                }
                BigDecimal fee = systemConfigService.getDecimalOrDefault(key, new BigDecimal("0.00"));
                appointment.setFee(fee);
                
                // 根据患者身份计算实际支付费用
                if (appointment.getActualFee() == null || appointment.getActualFee().compareTo(BigDecimal.ZERO) <= 0) {
                    BigDecimal actualFee = calculateActualFee(appointment.getPatientId(), fee);
                    appointment.setActualFee(actualFee);
                }
            }
        } catch (Exception e) {
            logger.warn("计算预约费用失败，使用默认0: {}", e.getMessage());
            if (appointment.getFee() == null) {
                appointment.setFee(new BigDecimal("0.00"));
            }
            if (appointment.getActualFee() == null) {
                appointment.setActualFee(appointment.getFee());
            }
        }

        // ========== 入库 ==========
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

        // 如果删除成功，尝试从候补队列中弹出下一个患者并创建预约；若无候补则归还号源
        if (result > 0) {
            boolean filled = processNextInWaitlist(appointment.getScheduleId());
            if (!filled) {
                try {
                    scheduleMapper.increaseAvailableSlots(appointment.getScheduleId());
                } catch (Exception e) {
                    logger.warn("归还号源失败: {}", e.getMessage());
                }
            }
        }

        return result;
    }

    /** [修改] 处理候补队列：为队首患者创建预约；返回是否已被候补填充 */
    public boolean processNextInWaitlist(Long scheduleId) {
        try {
            // [修改] 1. 从 Redis 弹出下一个患者 (返回 Long patientId)
            Long nextPatientId = waitlistService.popNext(scheduleId);

            // [修改] 2. 检查返回的 patientId
            if (nextPatientId != null) {

                // [新增] 3. 验证预支付状态
                PrepaymentOrder prepaymentOrder = null;
                try {
                    // 查找该患者在该排班的预支付订单
                    List<PrepaymentOrder> orders = prepaymentOrderService.getOrdersByPatientId(nextPatientId);
                    prepaymentOrder = orders.stream()
                        .filter(order -> order.getScheduleId().equals(scheduleId))
                        .filter(order -> "PAID".equals(order.getStatus()))
                        .findFirst()
                        .orElse(null);

                    if (prepaymentOrder == null) {
                        logger.warn("候补转预约失败：未找到已支付的预支付订单，患者ID={}, 排班ID={}", nextPatientId, scheduleId);
                        // 将患者重新加入候补队列末尾，给其时间完成支付
                        waitlistService.addToQueue(nextPatientId, scheduleId);
                        return false;
                    }
                } catch (Exception e) {
                    logger.error("验证预支付状态失败: {}", e.getMessage());
                    // 将患者重新加入候补队列
                    waitlistService.addToQueue(nextPatientId, scheduleId);
                    return false;
                }

                // 4. 为队首患者创建预约
                Appointment newAppointment = new Appointment();
                newAppointment.setPatientId(nextPatientId);
                newAppointment.setScheduleId(scheduleId);
                newAppointment.setStatus("scheduled"); // 直接设为已预约状态
                newAppointment.setSourceType("WAITLIST");

                // [修复] 必须设置预约时间
                Date now = new java.util.Date();
                newAppointment.setCreatedAt(now);
                newAppointment.setAppointmentTime(now);

                // 5. 从 schedule 表查询医生ID
                var schedule = scheduleMapper.selectById(scheduleId);
                if (schedule != null) {
                    newAppointment.setDoctorId(schedule.getDoctorId());
                }

                // 6. 扣减号源（如果还有余量）
                int decreased = scheduleMapper.decreaseAvailableSlots(scheduleId);
                if (decreased <= 0) {
                    logger.warn("候补创建失败：无可用号源 scheduleId={}", scheduleId);
                    // 将患者重新加入候补队列
                    waitlistService.addToQueue(nextPatientId, scheduleId);
                    return false;
                }

                // 7. 使用预支付订单中的费用信息
                newAppointment.setFee(prepaymentOrder.getOriginalFee());
                newAppointment.setActualFee(prepaymentOrder.getActualFee());

                // 8. 入库预约记录
                appointmentMapper.insert(newAppointment);

                // 9. 消费预支付订单
                try {
                    prepaymentOrderService.consumeOrder(prepaymentOrder.getWaitlistId());
                } catch (Exception e) {
                    logger.error("消费预支付订单失败: {}", e.getMessage());
                    // 这里可以考虑是否需要回滚预约创建，但为了简化流程，我们继续处理
                }

                // 10. 更新日志
                logger.info("候补队列自动创建预约成功: 患者ID={}, 排班ID={}, 订单号={}",
                        nextPatientId, scheduleId, prepaymentOrder.getOrderNo());
                return true;
            }
        } catch (Exception e) {
            // 记录日志但不影响主流程
            logger.error("处理候补队列失败: {}", e.getMessage(), e);
        }
        return false;
    }

    /**
     * 取消预约（更新状态为cancelled）
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

        // 退号时限：就诊前2小时内不可退号（可通过配置覆盖）
        int cancelLimitMinutes = systemConfigService.getIntOrDefault("CANCEL_LIMIT_MINUTES", 120);
        Date now = new Date();

        // [修复] 确保 appointmentTime 不为 null
        if (appointment.getAppointmentTime() == null) {
            logger.warn("预约 {} 的 appointmentTime 为 null，跳过退号时限检查", id);
        } else {
            long diffMillis = appointment.getAppointmentTime().getTime() - now.getTime();
            long remainMinutes = diffMillis / (60 * 1000);
            if (remainMinutes < cancelLimitMinutes) {
                throw new CustomerException("距离就诊不足" + cancelLimitMinutes + "分钟，不可退号");
            }
        }

        int result = appointmentMapper.updateStatus(id, "cancelled");

        // 如果取消成功，尝试从候补队列中弹出下一个患者并创建预约；若无候补则归还号源
        if (result > 0) {
            boolean filled = processNextInWaitlist(appointment.getScheduleId());
            if (!filled) {
                try {
                    scheduleMapper.increaseAvailableSlots(appointment.getScheduleId());
                } catch (Exception e) {
                    logger.warn("归还号源失败: {}", e.getMessage());
                }
            }
        }

        return result;
    }

    /** 根据患者 ID 查询预约列表 */
    public List<Appointment> listByPatient(Long patientId) {
        return appointmentMapper.selectByPatientId(patientId);
    }
    
    /** 根据患者 ID 查询预约列表（包含医生信息） */
    public List<AppointmentWithDoctorDTO> listByPatientWithDoctorInfo(Long patientId) {
        return appointmentMapper.selectByPatientIdWithDoctorInfo(patientId);
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

    // ===== 工具方法 =====
    
    /**
     * 根据患者身份计算实际支付费用
     */
    private BigDecimal calculateActualFee(Long patientId, BigDecimal originalFee) {
        try {
            Patient patient = patientMapper.selectById(patientId);
            if (patient == null) {
                return originalFee;
            }
            
            String specificRole = patient.getSpecificRole();
            if (specificRole == null || !"verified".equals(patient.getIdStatus())) {
                // 未认证或无身份信息，不享受报销
                return originalFee;
            }
            
            // 根据身份类型计算报销后的实际费用
            if ("student".equals(specificRole)) {
                // 学生报锅95%，实付5%
                return originalFee.multiply(new BigDecimal("0.05"));
            } else if ("teacher".equals(specificRole)) {
                // 教师报锅90%，实付10%
                return originalFee.multiply(new BigDecimal("0.10"));
            }
            
            return originalFee;
        } catch (Exception e) {
            logger.warn("计算实际费用失败: {}", e.getMessage());
            return originalFee;
        }
    }
    
    private Date atStartOfDay(Date date) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(date);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date atStartOfNextDay(Date date) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(date);
        cal.add(java.util.Calendar.DATE, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}