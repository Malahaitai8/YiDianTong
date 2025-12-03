package com.example.springboot.service;

import com.example.springboot.config.SecurityUtils;
import com.example.springboot.entity.Appointment;
import com.example.springboot.entity.Patient;
import com.example.springboot.entity.PrepaymentOrder;
import com.example.springboot.entity.Waitlist;
import com.example.springboot.entity.Schedule;
import com.example.springboot.exception.CustomerException;
import com.example.springboot.mapper.AppointmentMapper;
import com.example.springboot.mapper.PatientMapper;
import com.example.springboot.mapper.ScheduleMapper;
import com.example.springboot.mapper.WaitlistMapper;
import com.example.springboot.mapper.DoctorMapper;
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
import java.util.Calendar;

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

    @Resource
    private WaitlistMapper waitlistMapper;

    @Resource
    private NotificationService notificationService;

    @Resource
    private DoctorMapper doctorMapper;

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
        
        // [修复] 检查已预约数量，防止超售
        int existingAppointments = appointmentMapper.countByScheduleId(appointment.getScheduleId());
        int totalSlots = schedule.getTotalSlots() != null ? schedule.getTotalSlots() : 0;
        if (existingAppointments >= totalSlots) {
            throw new CustomerException("该排班的所有号源已被预约");
        }

        // ========== 扣减号源（原子性） ==========
        int decreased = scheduleMapper.decreaseAvailableSlots(schedule.getId());
        if (decreased <= 0) {
            throw new CustomerException("号源不足");
        }
        
        // [修复] 扣减号源后，再次检查是否已有相同患者的预约（防止并发重复预约）
        int duplicateCheck = appointmentMapper.existsByPatientAndSchedule(patientId, appointment.getScheduleId());
        if (duplicateCheck > 0) {
            // 归还号源
            scheduleMapper.increaseAvailableSlots(schedule.getId());
            throw new CustomerException("检测到重复预约，操作已取消");
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
        
        // ========== 发送预约成功通知 ==========
        try {
            Schedule scheduleInfo = scheduleMapper.selectById(appointment.getScheduleId());
            if (scheduleInfo != null) {
                com.example.springboot.entity.Doctor doctor = doctorMapper.selectById(scheduleInfo.getDoctorId());
                String doctorName = doctor != null ? doctor.getName() : "未知医生";
                String dateStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(scheduleInfo.getScheduleDate());
                String timeSlot = scheduleInfo.getTimeSlot();
                
                Patient patient = patientMapper.selectById(appointment.getPatientId());
                if (patient != null) {
                    notificationService.sendAppointmentSuccessNotification(
                        patient.getUserId(),
                        appointment.getPatientId(),
                        appointment.getId(),
                        doctorName,
                        dateStr,
                        timeSlot
                    );
                }
            }
        } catch (Exception e) {
            logger.warn("发送预约成功通知失败: {}", e.getMessage());
        }
        
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

        // 如果尚未退号（不是已取消），先执行一次标准退号流程，统一走 cancelById 的候补 / 号源逻辑
        if (!"cancelled".equalsIgnoreCase(appointment.getStatus())) {
            // 这里直接调用内部逻辑，避免重复的权限和时间校验
            cancelById(id);
        }

        // 删除预约（仅做数据清理，此时状态已经是 cancelled，不再影响号源）
        int result = appointmentMapper.deleteById(id);
        // 号源增减统一走 cancelById 流程；deleteById 只做物理删除，避免多次“归还号源”导致 available_slots 跳变

        return result;
    }

    /** [修改] 处理候补队列：为队首患者创建预约；返回是否已被候补填充 */
    @Transactional(rollbackFor = Exception.class)
    public boolean processNextInWaitlist(Long scheduleId) {
        // 只在需要排查问题时打开 DEBUG；正常运行不刷屏
        logger.debug("processNextInWaitlist start scheduleId={}", scheduleId);
        final int maxAttempts = 10;
        int attempts = 0;
        while (attempts < maxAttempts) {
            attempts++;
            Waitlist waitlistEntry = waitlistService.popNext(scheduleId);
            if (waitlistEntry == null) {
                logger.debug("processNextInWaitlist no candidate scheduleId={} attempts={}", scheduleId, attempts);
                return false;
            }
            boolean processed = processSingleWaitlistEntry(scheduleId, waitlistEntry);
            if (processed) {
                logger.info("processNextInWaitlist success scheduleId={} attempts={}", scheduleId, attempts);
                return true;
            }
        }
        logger.warn("多次尝试处理候补队列仍未成功，scheduleId={}, attempts={}", scheduleId, attempts);
        return false;
    }

    private boolean processSingleWaitlistEntry(Long scheduleId, Waitlist waitlistEntry) {
        try {
            logger.debug("processSingleWaitlistEntry start scheduleId={} waitlistId={}", scheduleId, waitlistEntry != null ? waitlistEntry.getId() : null);
            // [修复] 验证候补记录中的scheduleId是否与传入的scheduleId一致
            Long waitlistScheduleId = waitlistEntry.getScheduleId();
            if (!scheduleId.equals(waitlistScheduleId)) {
                logger.error("候补转预约失败：候补记录的scheduleId({})与传入的scheduleId({})不一致，waitlistId={}", 
                    waitlistScheduleId, scheduleId, waitlistEntry.getId());
                waitlistService.requeue(waitlistEntry);
                return false;
            }
            
            Long nextPatientId = waitlistEntry.getPatientId();
            
            // [修复] 在开始处理前，先检查患者是否已有该排班的预约（防止重复）
            int existingAppointment = appointmentMapper.existsByPatientAndSchedule(nextPatientId, scheduleId);
            if (existingAppointment > 0) {
                logger.warn("候补转预约失败：患者已存在该排班的预约，跳过处理 scheduleId={}, patientId={}, waitlistId={}", 
                    scheduleId, nextPatientId, waitlistEntry.getId());
                // 候补记录已从队列中弹出，但不需要重新入队（因为患者已有预约）
                // 更新候补状态为已处理
                try {
                    waitlistMapper.updateStatus(waitlistEntry.getId(), "GRANTED");
                } catch (Exception e) {
                    logger.warn("更新候补状态失败: {}", e.getMessage());
                }
                return false;
            }
            
            // [修复] 检查候补记录状态，如果已经是GRANTED，说明已经被处理过了
            if ("GRANTED".equalsIgnoreCase(waitlistEntry.getStatus())) {
                logger.warn("候补转预约失败：候补记录已被处理过 waitlistId={}, status={}", 
                    waitlistEntry.getId(), waitlistEntry.getStatus());
                return false;
            }
            
            logger.debug("候补转预约：验证通过 waitlistId={}, scheduleId={}, patientId={}",
                waitlistEntry.getId(), scheduleId, nextPatientId);

            // 3. 验证预支付状态
            PrepaymentOrder prepaymentOrder;
            try {
                prepaymentOrder = prepaymentOrderService.getOrderByWaitlistId(waitlistEntry.getId());
                if (prepaymentOrder == null || !"PAID".equals(prepaymentOrder.getStatus())) {
                    logger.warn("候补转预约失败：未找到可用的预支付订单，waitlistId={}, patientId={}, scheduleId={}",
                            waitlistEntry.getId(), nextPatientId, scheduleId);
                    waitlistService.requeue(waitlistEntry);
                    return false;
                }
            } catch (Exception e) {
                logger.error("验证预支付状态失败: {}", e.getMessage());
                waitlistService.requeue(waitlistEntry);
                return false;
            }

            // 4. 为队首患者创建预约
            Appointment newAppointment = new Appointment();
            newAppointment.setPatientId(nextPatientId);
            newAppointment.setScheduleId(scheduleId);
            newAppointment.setStatus("scheduled");
            newAppointment.setSourceType("WAITLIST");

            Date now = new Date();
            newAppointment.setCreatedAt(now);

            Schedule schedule = scheduleMapper.selectById(scheduleId);
            if (schedule != null) {
                newAppointment.setDoctorId(schedule.getDoctorId());
                // [修复] 根据排班的日期和时间段计算正确的预约时间
                Date appointmentTime = calculateAppointmentTimeFromSchedule(schedule.getScheduleDate(), schedule.getTimeSlot());
                newAppointment.setAppointmentTime(appointmentTime);
                logger.debug("候补转预约：设置预约时间为排班时间 scheduleDate={}, timeSlot={}, appointmentTime={}",
                    schedule.getScheduleDate(), schedule.getTimeSlot(), appointmentTime);
            } else {
                // 如果排班不存在，使用当前时间（不应该发生，但作为兜底）
                logger.warn("候补转预约：排班不存在，使用当前时间作为预约时间 scheduleId={}", scheduleId);
                newAppointment.setAppointmentTime(now);
            }

            // [修复] 在进行候补转预约前，基于总号源和未取消预约数检查容量
            int existingAppointments = appointmentMapper.countByScheduleId(scheduleId);
            Schedule currentSchedule = scheduleMapper.selectById(scheduleId);
            if (currentSchedule == null) {
                logger.error("候补转预约失败：排班不存在 scheduleId={}", scheduleId);
                waitlistService.requeue(waitlistEntry);
                return false;
            }

            int totalSlots = currentSchedule.getTotalSlots() != null ? currentSchedule.getTotalSlots() : 0;
            if (existingAppointments >= totalSlots) {
                logger.warn("候补转预约失败：该排班的所有号源已被预约 scheduleId={}, totalSlots={}, existingAppointments={}",
                    scheduleId, totalSlots, existingAppointments);
                waitlistService.requeue(waitlistEntry);
                return false;
            }

            // [规则调整] 不再依赖 available_slots 判断容量，只用于前端展示。
            // 对于候补转预约，无论当前 available_slots 为多少，只要未取消预约数 < totalSlots，就允许转预约。
            // 这里不再调用 decreaseAvailableSlots，而是根据业务规则在后续单独调整 available_slots。
            
            // [修复] 扣减号源后，再次检查是否已有相同患者的预约（防止并发重复）
            int duplicateCheck = appointmentMapper.existsByPatientAndSchedule(nextPatientId, scheduleId);
            if (duplicateCheck > 0) {
                logger.error("候补转预约失败：检测到重复预约（扣减号源后），归还号源 scheduleId={}, patientId={}, waitlistId={}", 
                    scheduleId, nextPatientId, waitlistEntry.getId());
                // 归还号源
                scheduleMapper.increaseAvailableSlots(scheduleId);
                // 候补记录已从队列中弹出，更新状态为已处理（避免重复处理）
                try {
                    waitlistMapper.updateStatus(waitlistEntry.getId(), "GRANTED");
                } catch (Exception e) {
                    logger.warn("更新候补状态失败: {}", e.getMessage());
                }
                return false;
            }

            newAppointment.setFee(prepaymentOrder.getOriginalFee());
            newAppointment.setActualFee(prepaymentOrder.getActualFee());

            // [修复] 在插入预约前，最后一次检查（防止并发问题）
            int finalCheck = appointmentMapper.existsByPatientAndSchedule(nextPatientId, scheduleId);
            if (finalCheck > 0) {
                logger.error("候补转预约失败：最终检查发现重复预约，归还号源并回滚 scheduleId={}, patientId={}, waitlistId={}", 
                    scheduleId, nextPatientId, waitlistEntry.getId());
                try {
                    waitlistMapper.updateStatus(waitlistEntry.getId(), "GRANTED");
                } catch (Exception e) {
                    logger.warn("更新候补状态失败: {}", e.getMessage());
                }
                return false;
            }

            appointmentMapper.insert(newAppointment);

            // ========== 依据新业务规则调整 available_slots ==========
            // 规则：无论排班是否已满，只要有候补成功顶上，都要“吃掉”一个可用号源：
            // - 如果原来 available_slots > 0，则减 1；
            // - 如果原来为 0，则保持 0（不允许负数）。
            try {
                Schedule beforeUpdate = scheduleMapper.selectById(scheduleId);
                if (beforeUpdate != null && beforeUpdate.getAvailableSlots() != null && beforeUpdate.getAvailableSlots() > 0) {
                    scheduleMapper.decreaseAvailableSlots(scheduleId);
                }
            } catch (Exception e) {
                logger.warn("候补转预约后调整 available_slots 失败 scheduleId={}, error={}", scheduleId, e.getMessage());
            }

            try {
                prepaymentOrderService.consumeOrder(waitlistEntry.getId());
            } catch (Exception e) {
                logger.error("消费预支付订单失败: {}", e.getMessage());
            }

            // [修复] 确保候补状态更新成功，如果失败则记录错误
            try {
                int updateResult = waitlistMapper.updateStatus(waitlistEntry.getId(), "GRANTED");
                if (updateResult <= 0) {
                    logger.error("候补状态更新失败：影响行数为0 waitlistId={}", waitlistEntry.getId());
                } else {
                    // [修复] 候补状态更新成功后，从patient索引中移除候补记录，避免前端显示重复
                    try {
                        waitlistService.removeFromQueue(waitlistEntry);
                        logger.info("候补转预约成功：已从队列中移除候补记录 waitlistId={}", waitlistEntry.getId());
                    } catch (Exception e) {
                        logger.warn("从队列中移除候补记录失败: waitlistId={}, error={}", waitlistEntry.getId(), e.getMessage());
                    }
                }
            } catch (Exception e) {
                logger.error("更新候补状态异常: waitlistId={}, error={}", waitlistEntry.getId(), e.getMessage(), e);
            }

            // 清除候补统计数据缓存，确保数据实时性
            try {
                waitlistService.invalidateStatsCacheBySchedule(scheduleId);
            } catch (Exception e) {
                logger.warn("清除候补统计缓存失败: {}", e.getMessage());
            }

            // ========== 发送候补成功通知 ==========
            try {
                Schedule scheduleInfo = scheduleMapper.selectById(scheduleId);
                if (scheduleInfo != null) {
                    com.example.springboot.entity.Doctor doctor = doctorMapper.selectById(scheduleInfo.getDoctorId());
                    String doctorName = doctor != null ? doctor.getName() : "未知医生";
                    String dateStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(scheduleInfo.getScheduleDate());
                    String timeSlot = scheduleInfo.getTimeSlot();
                    
                    Patient patient = patientMapper.selectById(nextPatientId);
                    if (patient != null) {
                        // 发送微信订阅消息通知
                        notificationService.sendWaitlistSuccessNotification(
                            patient.getUserId(),
                            nextPatientId,
                            newAppointment.getId(),
                            doctorName,
                            dateStr,
                            timeSlot
                        );
                        
                        // 实时推送WebSocket消息（包含scheduleId和appointmentId以便前端匹配）
                        com.example.springboot.websocket.WaitlistWebSocketServer.pushWaitlistSuccess(
                            patient.getUserId(),
                            scheduleId,
                            newAppointment.getId(),
                            doctorName,
                            dateStr,
                            timeSlot
                        );
                    }
                }
            } catch (Exception e) {
                logger.warn("发送候补成功通知失败: {}", e.getMessage());
            }

            logger.info("候补队列自动创建预约成功: 患者ID={}, 排班ID={}, 预约ID={}, 订单号={}, waitlistId={}",
                    nextPatientId, scheduleId, newAppointment.getId(), prepaymentOrder.getOrderNo(), waitlistEntry.getId());
            return true;
        } catch (Exception e) {
            logger.error("处理候补记录失败 waitlistId={}, scheduleId={}, error={}", 
                waitlistEntry != null ? waitlistEntry.getId() : null, scheduleId, e.getMessage(), e);
            return false;
        }
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

        // 如果取消成功，优先尝试用候补顶上；只有没有候补成功时才真正归还号源
        if (result > 0) {
            logger.info("cancelById -> scheduleId={} appointmentId={} start processNextInWaitlist", appointment.getScheduleId(), id);
            boolean filled = processNextInWaitlist(appointment.getScheduleId());
            logger.info("cancelById -> scheduleId={} appointmentId={} filled={}", appointment.getScheduleId(), id, filled);
            if (!filled) {
                // 无候补成功时，再归还号源并推送“号源释放”通知
                try {
                    scheduleMapper.increaseAvailableSlots(appointment.getScheduleId());
                    logger.info("cancelById -> scheduleId={} 无候补成功，归还号源完成", appointment.getScheduleId());

                    Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
                    if (schedule != null) {
                        com.example.springboot.entity.Doctor doctor = doctorMapper.selectById(schedule.getDoctorId());
                        String doctorName = doctor != null ? doctor.getName() : "未知医生";
                        String dateStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(schedule.getScheduleDate());

                        com.example.springboot.websocket.WaitlistWebSocketServer.pushSlotAvailable(
                            appointment.getScheduleId(),
                            doctorName,
                            dateStr,
                            schedule.getTimeSlot()
                        );
                    }
                } catch (Exception e) {
                    logger.warn("取消预约归还号源失败: {}", e.getMessage());
                }
            }
            // ========== 发送取消预约通知 ==========
            try {
                Schedule scheduleInfo = scheduleMapper.selectById(appointment.getScheduleId());
                if (scheduleInfo != null) {
                    com.example.springboot.entity.Doctor doctor = doctorMapper.selectById(scheduleInfo.getDoctorId());
                    String doctorName = doctor != null ? doctor.getName() : "未知医生";
                    String dateStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(scheduleInfo.getScheduleDate());
                    
                    Patient patient = patientMapper.selectById(appointment.getPatientId());
                    if (patient != null) {
                        notificationService.sendAppointmentCancelNotification(
                            patient.getUserId(),
                            appointment.getPatientId(),
                            appointment.getId(),
                            doctorName,
                            dateStr
                        );
                    }
                }
            } catch (Exception e) {
                logger.warn("发送取消预约通知失败: {}", e.getMessage());
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
        List<AvailableSlotDTO> slots = scheduleMapper.searchAvailableSlots(departmentId, doctorId, startDate, endDate, timeSlot);
        if (slots == null) {
            return java.util.Collections.emptyList();
        }

        // 按后端统一收费规则（system_config）计算每个号源的挂号费
        for (AvailableSlotDTO slot : slots) {
            String normalized = slot.getSlotType() == null ? "NORMAL" : slot.getSlotType().trim().toUpperCase();
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
            java.math.BigDecimal fee = systemConfigService.getDecimalOrDefault(key, java.math.BigDecimal.ZERO);
            slot.setFee(fee.doubleValue());
        }

        return slots;
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
    
    /**
     * 根据排班日期和时间段计算预约时间
     * @param scheduleDate 排班日期
     * @param timeSlot 时间段 (morning/afternoon/evening)
     * @return 预约时间（排班日期的具体时间点）
     */
    private Date calculateAppointmentTimeFromSchedule(Date scheduleDate, String timeSlot) {
        if (scheduleDate == null) {
            return new Date();
        }
        
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(scheduleDate);
        
        // 根据时间段设置默认时间
        if (timeSlot != null) {
            String normalized = timeSlot.trim().toLowerCase();
            switch (normalized) {
                case "morning":
                case "上午":
                    cal.set(java.util.Calendar.HOUR_OF_DAY, 9); // 上午9点
                    cal.set(java.util.Calendar.MINUTE, 0);
                    break;
                case "afternoon":
                case "下午":
                    cal.set(java.util.Calendar.HOUR_OF_DAY, 15); // 下午3点
                    cal.set(java.util.Calendar.MINUTE, 0);
                    break;
                case "evening":
                case "晚上":
                    cal.set(java.util.Calendar.HOUR_OF_DAY, 19); // 晚上7点
                    cal.set(java.util.Calendar.MINUTE, 0);
                    break;
                default:
                    cal.set(java.util.Calendar.HOUR_OF_DAY, 9); // 默认上午9点
                    cal.set(java.util.Calendar.MINUTE, 0);
            }
        } else {
            cal.set(java.util.Calendar.HOUR_OF_DAY, 9); // 默认上午9点
            cal.set(java.util.Calendar.MINUTE, 0);
        }
        
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        
        return cal.getTime();
    }
}