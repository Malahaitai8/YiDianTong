package com.example.springboot.service;

import com.example.springboot.dto.*;
import com.example.springboot.entity.*;
import com.example.springboot.mapper.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Date;
import java.util.Calendar;
import com.example.springboot.websocket.WaitlistWebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 申请记录服务
 */
@Service
public class ApplicationRequestService {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationRequestService.class);
    // Testing flag: when true, simulate refunds immediately instead of integrating real payment gateway.
    private static final boolean SIMULATE_REFUND = true;

    @Resource
    private ApplicationRequestMapper applicationRequestMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private DoctorMapper doctorMapper;

    @Resource
    private ClinicMapper clinicMapper;

    @Resource
    private ScheduleMapper scheduleMapper;
    
    @Resource
    private AppointmentMapper appointmentMapper;
    
    @Resource
    private com.example.springboot.mapper.AppointmentMigrationAuditMapper appointmentMigrationAuditMapper;
    
    @Resource
    private com.example.springboot.mapper.PrepaymentOrderMapper prepaymentOrderMapper;
    
    @Resource
    private com.example.springboot.mapper.WaitlistMapper waitlistMapper;

    @Resource
    private AppointmentService appointmentService;

    @Resource
    private NotificationService notificationService;

    @Resource
    private com.example.springboot.mapper.PatientMapper patientMapper;

    /**
     * 创建申请
     */
    @Transactional
    public ApplicationRequestDetailDTO createRequest(CreateApplicationRequestDTO dto, Long applicantId, String applicantRole) {
        // 1. 验证申请类型和必填字段
        if ("SCHEDULE_CHANGE".equals(dto.getRequestType())) {
            if (dto.getScheduleId() == null) {
                throw new RuntimeException("调班申请必须指定排班ID");
            }
            if (dto.getChangeType() == null) {
                throw new RuntimeException("调班申请必须指定调班类型");
            }
            
            // 验证排班是否存在
            Schedule schedule = scheduleMapper.selectById(dto.getScheduleId());
            if (schedule == null) {
                throw new RuntimeException("排班不存在");
            }
        } else if ("INFO_UPDATE".equals(dto.getRequestType())) {
            if (dto.getDoctorId() == null) {
                throw new RuntimeException("信息修改申请必须指定医生ID");
            }
            if (dto.getFieldName() == null) {
                throw new RuntimeException("信息修改申请必须指定字段名");
            }
            if (dto.getNewValue() == null) {
                throw new RuntimeException("信息修改申请必须指定新值");
            }
            
            // 验证医生是否存在
            Doctor doctor = doctorMapper.selectById(dto.getDoctorId());
            if (doctor == null) {
                throw new RuntimeException("医生不存在");
            }
            
            // 获取旧值
            String oldValue = getDoctorFieldValue(doctor, dto.getFieldName());
            if (oldValue == null) {
                throw new RuntimeException("字段名无效");
            }
            
            // 验证新值不能和旧值相同
            if (dto.getNewValue() != null && dto.getNewValue().trim().equals(oldValue.trim())) {
                throw new RuntimeException("新值不能和旧值相同");
            }
        }

        // 2. 构建实体对象
        ApplicationRequest request = new ApplicationRequest();
        request.setRequestType(dto.getRequestType());
        request.setApplicantId(applicantId);
        request.setApplicantRole(applicantRole);
        request.setScheduleId(dto.getScheduleId());
        request.setChangeType(dto.getChangeType());
        request.setNewDate(dto.getNewDate());
        request.setNewTimeSlot(dto.getNewTimeSlot());
        request.setSlotAdjustment(dto.getSlotAdjustment());
        request.setDoctorId(dto.getDoctorId());
        request.setFieldName(dto.getFieldName());
        request.setNewValue(dto.getNewValue());
        request.setReason(dto.getReason());
        request.setStatus("PENDING");

        // 设置原始信息
        if ("SCHEDULE_CHANGE".equals(dto.getRequestType())) {
            Schedule schedule = scheduleMapper.selectById(dto.getScheduleId());
            request.setOriginalDate(schedule.getScheduleDate());
            request.setOriginalTimeSlot(schedule.getTimeSlot());
        } else if ("INFO_UPDATE".equals(dto.getRequestType())) {
            Doctor doctor = doctorMapper.selectById(dto.getDoctorId());
            request.setOldValue(getDoctorFieldValue(doctor, dto.getFieldName()));
        }

        // 3. 保存到数据库
        applicationRequestMapper.insert(request);

        // 4. 返回详情
        return convertToDetailDTO(applicationRequestMapper.selectById(request.getId()));
    }

    /**
     * 查询所有申请
     */
    public List<ApplicationRequestDetailDTO> getAllRequests() {
        List<ApplicationRequest> requests = applicationRequestMapper.selectAll();
        return requests.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID查询申请
     */
    public ApplicationRequestDetailDTO getRequestById(Long id) {
        ApplicationRequest request = applicationRequestMapper.selectById(id);
        if (request == null) {
            throw new RuntimeException("申请不存在");
        }
        return convertToDetailDTO(request);
    }

    /**
     * 根据ID查询申请详情（直接从数据库联查）
     */
    public ApplicationRequestDetailDTO getRequestDetailById(Long id) {
        ApplicationRequestDetailDTO detail = applicationRequestMapper.selectDetailById(id);
        if (detail == null) {
            throw new RuntimeException("申请不存在");
        }
        return detail;
    }

    /**
     * 根据申请人查询
     */
    public List<ApplicationRequestDetailDTO> getRequestsByApplicant(Long applicantId) {
        List<ApplicationRequest> requests = applicationRequestMapper.selectByApplicantId(applicantId);
        return requests.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据状态查询
     */
    public List<ApplicationRequestDetailDTO> getRequestsByStatus(String status) {
        List<ApplicationRequest> requests = applicationRequestMapper.selectByStatus(status);
        return requests.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * 查询待审核的申请
     */
    public List<ApplicationRequestDetailDTO> getPendingRequests() {
        List<ApplicationRequest> requests = applicationRequestMapper.selectPendingRequests();
        return requests.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * 审核申请
     */
    @Transactional
    public ApplicationRequestDetailDTO reviewRequest(ReviewApplicationRequestDTO dto, Long reviewerId) {
        // 1. 查询申请
        ApplicationRequest request = applicationRequestMapper.selectById(dto.getRequestId());
        if (request == null) {
            throw new RuntimeException("申请不存在");
        }

        // 2. 检查申请状态
        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("申请已经被处理");
        }

        // 3. 检查拒绝原因
        if ("REJECTED".equals(dto.getAction()) && 
            (dto.getRejectReason() == null || dto.getRejectReason().trim().isEmpty())) {
            throw new RuntimeException("拒绝申请必须填写拒绝原因");
        }

        // 4. 如果是批准，执行相应的操作
        if ("APPROVED".equals(dto.getAction())) {
            if ("SCHEDULE_CHANGE".equals(request.getRequestType())) {
                handleScheduleChangeApproval(request);
            } else if ("INFO_UPDATE".equals(request.getRequestType())) {
                handleInfoUpdateApproval(request);
            }
        }

        // 5. 更新申请状态
        applicationRequestMapper.updateStatus(
                dto.getRequestId(),
                dto.getAction(),
                reviewerId,
                new Date(),
                dto.getRejectReason()
        );

        // 6. 返回详情
        return convertToDetailDTO(applicationRequestMapper.selectById(dto.getRequestId()));
    }

    /**
     * 取消申请
     */
    @Transactional
    public void cancelRequest(Long requestId, Long applicantId) {
        ApplicationRequest request = applicationRequestMapper.selectById(requestId);
        if (request == null) {
            throw new RuntimeException("申请不存在");
        }

        if (!request.getApplicantId().equals(applicantId)) {
            throw new RuntimeException("只能取消自己的申请");
        }

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("只能取消待审核的申请");
        }

        applicationRequestMapper.updateStatus(requestId, "CANCELLED", null, new Date(), "申请人取消");
    }

    /**
     * 删除申请
     */
    @Transactional
    public void deleteRequest(Long id) {
        ApplicationRequest request = applicationRequestMapper.selectById(id);
        if (request == null) {
            throw new RuntimeException("申请不存在");
        }
        applicationRequestMapper.deleteById(id);
    }

    /**
     * 统计申请数量
     */
    public Map<String, Integer> getRequestStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("pending", applicationRequestMapper.countByStatus("PENDING"));
        stats.put("approved", applicationRequestMapper.countByStatus("APPROVED"));
        stats.put("rejected", applicationRequestMapper.countByStatus("REJECTED"));
        stats.put("cancelled", applicationRequestMapper.countByStatus("CANCELLED"));
        stats.put("total", applicationRequestMapper.selectAll().size());
        return stats;
    }

    // ========== 私有辅助方法 ==========

    /**
     * 处理调班申请批准
     */
    private void handleScheduleChangeApproval(ApplicationRequest request) {
        Schedule schedule = scheduleMapper.selectById(request.getScheduleId());
        if (schedule == null) {
            throw new RuntimeException("排班不存在");
        }

        switch (request.getChangeType()) {
            case "CANCEL":
                // 取消排班 - 需要检查是否有患者预约，如果有则需要重新安排
                handleScheduleCancellation(schedule);
                break;
                
            case "RESCHEDULE":
                // 改期 - 需要检查是否有患者预约，如果有则需要重新安排
                handleScheduleReschedule(schedule, request.getNewDate(), request.getNewTimeSlot());
                break;
                
            case "ADJUST_SLOTS":
                // 调整号源
                if (request.getSlotAdjustment() == null) {
                    throw new RuntimeException("调整号源申请必须指定调整数量");
                }
                int newTotalSlots = schedule.getTotalSlots() + request.getSlotAdjustment();
                int newAvailableSlots = schedule.getAvailableSlots() + request.getSlotAdjustment();
                
                if (newTotalSlots < 0 || newAvailableSlots < 0) {
                    throw new RuntimeException("号源数量不能为负数");
                }
                
                schedule.setTotalSlots(newTotalSlots);
                schedule.setAvailableSlots(newAvailableSlots);
                scheduleMapper.updateById(schedule);
                break;
                
            default:
                throw new RuntimeException("未知的调班类型: " + request.getChangeType());
        }
    }

    /**
     * 处理排班改期，重新安排已预约的患者
     */
    private void handleScheduleReschedule(Schedule schedule, Date newDate, String newTimeSlot) {
        if (newDate == null || newTimeSlot == null) {
                    throw new RuntimeException("改期申请必须指定新日期和新时间段");
                }

        // 1. 检查是否有患者预约了原排班
        List<Appointment> affectedAppointments = appointmentMapper.selectByScheduleId(schedule.getId())
            .stream()
            .filter(apt -> !"CANCELLED".equals(apt.getStatus()))
            .collect(Collectors.toList());

        if (affectedAppointments.isEmpty()) {
            // 如果没有患者预约，直接更新排班时间
            schedule.setScheduleDate(newDate);
            schedule.setTimeSlot(newTimeSlot);
                scheduleMapper.updateById(schedule);
            return;
        }

        // 2. 如果有预约，需要重新安排
        // 首先检查医生自己是否在新时间有排班（医生改期到自己的其他时间）
        Schedule existingNewSchedule = scheduleMapper.findScheduleByDoctorDateTime(
            schedule.getDoctorId(), newDate, newTimeSlot);

        Schedule targetSchedule;
        if (existingNewSchedule != null) {
            // 如果新时间医生已有排班，直接使用
            targetSchedule = existingNewSchedule;
            logger.info("医生改期：使用医生现有的新时间排班 scheduleId={}", targetSchedule.getId());
        } else {
            // 如果新时间没有排班，创建新的排班
            Schedule newSchedule = new Schedule();
            newSchedule.setDoctorId(schedule.getDoctorId());
            newSchedule.setScheduleDate(newDate);
            newSchedule.setTimeSlot(newTimeSlot);
            newSchedule.setSlotType(schedule.getSlotType());
            newSchedule.setTotalSlots(schedule.getTotalSlots());
            newSchedule.setAvailableSlots(schedule.getTotalSlots()); // 新排班初始号源为满
            scheduleMapper.insert(newSchedule);
            targetSchedule = newSchedule;
            logger.info("医生改期：创建新的排班 scheduleId={}", targetSchedule.getId());
        }

        // 3. 重新安排患者到新排班
        for (Appointment oldAppointment : affectedAppointments) {
            // 检查新排班是否有可用号源，且对应预约时间为未来（避免分配到已过时段）
            Date now = new Date();
            Date targetAptTime = calculateAppointmentTimeFromSchedule(targetSchedule.getScheduleDate(), targetSchedule.getTimeSlot());
            boolean targetTimeInFuture = targetAptTime != null && targetAptTime.after(now);

            if (targetSchedule.getAvailableSlots() > 0 && targetTimeInFuture) {
                // 更新原有预约记录，改为指向新排班
                oldAppointment.setScheduleId(targetSchedule.getId());
                oldAppointment.setDoctorId(targetSchedule.getDoctorId());
                oldAppointment.setSourceType("RESCHEDULED"); // 标记为改期重新安排
                // 标记重新安排时间为现在，患者重新选择窗口以此为准
                oldAppointment.setCreatedAt(new Date());

                // 计算新的预约时间（已为未来时间）
                oldAppointment.setAppointmentTime(targetAptTime);

                appointmentMapper.updateById(oldAppointment);
                scheduleMapper.decreaseAvailableSlots(targetSchedule.getId());

                // 发送改期重新安排通知
                sendRescheduleNotification(oldAppointment, schedule, targetSchedule);
            } else {
                // 如果目标排班的时间不在未来（例如当天已过的时段），不要直接分配，转为查找替代排班
                if (targetSchedule.getAvailableSlots() > 0 && !targetTimeInFuture) {
                    logger.info("目标排班时间已过，跳过直接分配 scheduleId={}, aptTime={}", targetSchedule.getId(), targetAptTime);
                }
                // 新排班没有号源 -> 按新规则查找替代排班：
                // 规则1：同科室 + 同医生 + 未来时间最近 + 同号别
                // 规则2：同科室 + 同号别医生 + 未来时间最近或者同时间
                // 规则3：如果科室没号了 → 直接退款并通知
                try {
                    // 获取原排班的科室ID
                    Doctor doctor = doctorMapper.selectById(schedule.getDoctorId());
                    if (doctor == null || doctor.getClinicId() == null) {
                        logger.warn("无法获取原排班的科室信息，跳过自动分配 scheduleId={}", schedule.getId());
                    } else {
                        Clinic clinic = clinicMapper.selectById(doctor.getClinicId());
                        Long departmentId = (clinic != null) ? clinic.getDepartmentId() : null;

                        if (departmentId == null) {
                            logger.warn("无法获取科室ID，跳过自动分配 scheduleId={}", schedule.getId());
                        } else {
                            String origSlotType = schedule.getSlotType();
                            Date todayStart = atStartOfDay(new Date());
                            Date searchStart = newDate != null && newDate.after(todayStart) ? newDate : todayStart;

                            // 若为当天但时间段已过，则从明天开始
                            try {
                                int origOrder = timeSlotOrder(newTimeSlot);
                                int nowOrder = timeSlotOrderFromNow();
                                if (searchStart.equals(todayStart) && origOrder > 0 && origOrder < nowOrder) {
                                    searchStart = atStartOfNextDay(todayStart);
                                }
                            } catch (Exception ex) {
                                // ignore
                            }

                            AvailableSlotDTO chosen = null;

                            // 规则1：同科室 + 同医生 + 未来时间最近 + 同号别
                            List<AvailableSlotDTO> candidates1 = scheduleMapper.searchAvailableSlots(
                                departmentId, // 同科室
                                schedule.getDoctorId(), // 同医生
                                searchStart,
                                new Date(searchStart.getTime() + 30L * 24 * 60 * 60 * 1000),
                                null, // 任何时间段（优先未来时间最近）
                                schedule.getId() // exclude original schedule
                            );

                            // 过滤掉患者已预约的排班，且必须同号别
                            List<AvailableSlotDTO> filtered1 = candidates1.stream()
                                .filter(s -> appointmentMapper.existsByPatientAndSchedule(oldAppointment.getPatientId(), s.getScheduleId()) == 0)
                                .filter(s -> s.getSlotType() != null && origSlotType != null && s.getSlotType().trim().equalsIgnoreCase(origSlotType.trim()))
                                .sorted((a, b) -> {
                                    // 按日期和时间段排序，优先最近的
                                    int dateCompare = a.getDate().compareTo(b.getDate());
                                    if (dateCompare != 0) return dateCompare;
                                    return timeSlotOrder(a.getTimeSlot()) - timeSlotOrder(b.getTimeSlot());
                                })
                                .collect(Collectors.toList());
                            // 排除已经过去的候选排班（避免分配到过去时间）
                            filtered1 = filtered1.stream()
                                .filter(s -> {
                                    Date aptTime = calculateAppointmentTimeFromSchedule(s.getDate(), s.getTimeSlot());
                                    return aptTime != null && aptTime.after(now);
                                })
                                .collect(Collectors.toList());

                            if (!filtered1.isEmpty()) {
                                chosen = filtered1.get(0);
                                logger.info("规则1匹配成功：同科室同医生同号别，scheduleId={}", chosen.getScheduleId());
                            }

                            // 规则2：如果规则1不行 → 同科室 + 同号别医生 + 未来时间最近或者同时间
                            if (chosen == null) {
                                List<AvailableSlotDTO> candidates2 = scheduleMapper.searchAvailableSlots(
                                    departmentId, // 同科室
                                    null, // 任何医生
                                    searchStart,
                                    new Date(searchStart.getTime() + 30L * 24 * 60 * 60 * 1000),
                                    newTimeSlot, // 同时间段优先，或未来时间最近
                                    schedule.getId() // exclude original schedule
                                );

                                // 过滤掉患者已预约的排班，且必须同号别
                                List<AvailableSlotDTO> filtered2 = candidates2.stream()
                                    .filter(s -> appointmentMapper.existsByPatientAndSchedule(oldAppointment.getPatientId(), s.getScheduleId()) == 0)
                                    .filter(s -> s.getSlotType() != null && origSlotType != null && s.getSlotType().trim().equalsIgnoreCase(origSlotType.trim()))
                                    .sorted((a, b) -> {
                                        // 优先同时间段，其次按日期时间排序
                                        if (newTimeSlot.equals(a.getTimeSlot()) && !newTimeSlot.equals(b.getTimeSlot())) {
                                            return -1;
                                        } else if (!newTimeSlot.equals(a.getTimeSlot()) && newTimeSlot.equals(b.getTimeSlot())) {
                                            return 1;
                                        }
                                        int dateCompare = a.getDate().compareTo(b.getDate());
                                        if (dateCompare != 0) return dateCompare;
                                        return timeSlotOrder(a.getTimeSlot()) - timeSlotOrder(b.getTimeSlot());
                                    })
                                    .collect(Collectors.toList());
                                // 过滤掉过去时间的候选
                                filtered2 = filtered2.stream()
                                    .filter(s -> {
                                        Date aptTime = calculateAppointmentTimeFromSchedule(s.getDate(), s.getTimeSlot());
                                        return aptTime != null && aptTime.after(now);
                                    })
                                    .collect(Collectors.toList());

                                if (!filtered2.isEmpty()) {
                                    chosen = filtered2.get(0);
                                    logger.info("规则2匹配成功：同科室同号别医生，scheduleId={}", chosen.getScheduleId());
                                }
                            }

                            if (chosen != null) {
                                // 将预约迁移到 chosen
                                Schedule chosenSchedule = scheduleMapper.selectById(chosen.getScheduleId());
                                if (chosenSchedule != null) {
                                    oldAppointment.setScheduleId(chosenSchedule.getId());
                                    oldAppointment.setDoctorId(chosenSchedule.getDoctorId());
                                    oldAppointment.setSourceType("RESCHEDULED");
                                    oldAppointment.setCreatedAt(new Date());
                                    oldAppointment.setAppointmentTime(calculateAppointmentTimeFromSchedule(chosenSchedule.getScheduleDate(), chosenSchedule.getTimeSlot()));
                                    appointmentMapper.updateById(oldAppointment);
                                    scheduleMapper.decreaseAvailableSlots(chosenSchedule.getId());
                                    sendRescheduleNotification(oldAppointment, schedule, chosenSchedule);
                                    continue; // 处理下一个 appointment
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    logger.warn("尝试查找替代排班失败 scheduleId={}, error={}", schedule.getId(), ex.getMessage());
                }

                // 无可用替代 -> 取消并退款
                oldAppointment.setStatus("CANCELLED");
                appointmentMapper.updateById(oldAppointment);

                // 发送退款通知
                sendRescheduleRefundNotification(oldAppointment, schedule, newDate, newTimeSlot);
            }
        }

        // 4. 处理原排班关联的预支付订单：将匹配到的订单迁移到新排班（按患者匹配），没有匹配的则标记为待退款
        try {
            List<PrepaymentOrder> relatedOrders = prepaymentOrderMapper.selectByScheduleId(schedule.getId());
            if (relatedOrders != null && !relatedOrders.isEmpty()) {
                for (PrepaymentOrder order : relatedOrders) {
                    // 若订单属于已经被重新分配的患者，则迁移到新排班
                    boolean migrated = false;
                    for (Appointment apt : affectedAppointments) {
                            if (apt.getPatientId() != null && apt.getPatientId().equals(order.getPatientId())) {
                            prepaymentOrderMapper.updateScheduleId(order.getId(), targetSchedule.getId());
                            // 清理 waitlist_id，订单已迁移为正式排班订单
                            try {
                                prepaymentOrderMapper.clearWaitlistId(order.getId());
                            } catch (Exception e) {
                                logger.warn("清除waitlist_id失败，订单已迁移但保留原waitlist_id: orderId={}, error={}", order.getId(), e.getMessage());
                                // 不抛出异常，继续处理
                            }
                            migrated = true;
                                            break;
                                        }
                    }
                            if (!migrated) {
                        // 标记为待退款并清理候补引用
                        prepaymentOrderMapper.updateRefundInfo(order.getId(), "TO_REFUND", null, "原排班改期，订单待退款");
                        try {
                            prepaymentOrderMapper.clearWaitlistId(order.getId());
                        } catch (Exception e) {
                            logger.warn("清除waitlist_id失败，订单标记退款但保留原waitlist_id: orderId={}, error={}", order.getId(), e.getMessage());
                            // 不抛出异常，继续处理
                        }

                        // 如果处于测试模式，立即模拟退款并通知患者（避免真实支付依赖）
                        if (SIMULATE_REFUND) {
                            try {
                                prepaymentOrderMapper.updateRefundInfo(order.getId(), "REFUNDED", new Date(), "模拟退款：改期无可用安排");
                                if (order.getPatientId() != null) {
                                    Patient p = patientMapper.selectById(order.getPatientId());
                                    if (p != null) {
                                        Long userId = p.getUserId();
                                        // 发送退款通知（使用原排班时间作为参考）
                                        notificationService.sendAppointmentCancelledRefundNotification(
                                            userId,
                                            order.getPatientId(),
                                            null,
                                            schedule.getScheduleDate().toString(),
                                            schedule.getTimeSlot()
                                        );
                                        WaitlistWebSocketServer.pushAppointmentCancelledRefund(
                                            userId,
                                            null,
                                            schedule.getScheduleDate().toString(),
                                            schedule.getTimeSlot()
                                        );
                                    }
                                }
                            } catch (Exception ex) {
                                logger.error("模拟退款失败 orderId={}, error={}", order.getId(), ex.getMessage(), ex);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("处理预支付订单迁移/退款失败 scheduleId={}, error={}", schedule.getId(), e.getMessage(), e);
            // 不中断主流程，但记录日志供排查
        }

        // 5. 处理该排班下的候补关联预支付订单（通过 waitlist_id 关联），并取消候补记录，避免外键约束
        try {
            List<Waitlist> waitlists = waitlistMapper.selectByScheduleId(schedule.getId());
            if (waitlists != null && !waitlists.isEmpty()) {
                for (Waitlist wl : waitlists) {
                    // 处理候补对应的预支付订单
                    List<PrepaymentOrder> wlOrders = prepaymentOrderMapper.selectByWaitlistId(wl.getId());
                    if (wlOrders != null && !wlOrders.isEmpty()) {
                        for (PrepaymentOrder order : wlOrders) {
                            boolean migrated = false;
                            for (Appointment apt : affectedAppointments) {
                            if (apt.getPatientId() != null && apt.getPatientId().equals(order.getPatientId())) {
                                    prepaymentOrderMapper.updateScheduleId(order.getId(), apt.getScheduleId());
                                    // 清理 waitlist_id
                                    try {
                                        prepaymentOrderMapper.clearWaitlistId(order.getId());
                                    } catch (Exception e) {
                                        logger.warn("清除waitlist_id失败，订单已迁移但保留原waitlist_id: orderId={}, error={}", order.getId(), e.getMessage());
                                        // 不抛出异常，继续处理
                                    }
                                    migrated = true;
                                            break;
                                        }
                            }
                            if (!migrated) {
                                prepaymentOrderMapper.updateRefundInfo(order.getId(), "TO_REFUND", null, "原排班改期，候补订单待退款");
                                try {
                                    prepaymentOrderMapper.clearWaitlistId(order.getId());
                                } catch (Exception e) {
                                    logger.warn("清除waitlist_id失败，候补订单标记退款但保留原waitlist_id: orderId={}, error={}", order.getId(), e.getMessage());
                                    // 不抛出异常，继续处理
                                }
                                if (SIMULATE_REFUND) {
                                    try {
                                        prepaymentOrderMapper.updateRefundInfo(order.getId(), "REFUNDED", new Date(), "模拟退款：候补订单改期无可用安排");
                                        if (order.getPatientId() != null) {
                                            Patient p = patientMapper.selectById(order.getPatientId());
                                            if (p != null) {
                                                notificationService.sendAppointmentCancelledRefundNotification(
                                                    p.getUserId(),
                                                    order.getPatientId(),
                                                    null,
                                                    schedule.getScheduleDate().toString(),
                                                    schedule.getTimeSlot()
                                                );
                                                WaitlistWebSocketServer.pushAppointmentCancelledRefund(
                                                    p.getUserId(),
                                                    null,
                                                    schedule.getScheduleDate().toString(),
                                                    schedule.getTimeSlot()
                                                );
                                            }
                                        }
                                    } catch (Exception ex) {
                                        logger.error("模拟退款失败（候补订单） orderId={}, error={}", order.getId(), ex.getMessage(), ex);
                                    }
                                }
                            }
                        }
                    }
                    // 标记候补记录为取消，避免残留外键引用
                    try {
                        waitlistMapper.updateStatus(wl.getId(), "CANCELLED");
                    } catch (Exception ex) {
                        logger.warn("更新候补状态失败 waitlistId={}, error={}", wl.getId(), ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("处理候补关联预支付订单失败 scheduleId={}, error={}", schedule.getId(), e.getMessage(), e);
        }

        // 6. 删除原排班（现在没有appointment或候补引用它了），使用安全删除封装
        safeDeleteSchedule(schedule.getId());
    }

    /**
     * 处理排班取消，重新安排已预约的患者
     */
    private void handleScheduleCancellation(Schedule schedule) {
        // 1. 查询该排班的所有有效预约
        List<Appointment> affectedAppointments = appointmentMapper.selectByScheduleId(schedule.getId())
            .stream()
            .filter(apt -> !"CANCELLED".equals(apt.getStatus()))
            .collect(Collectors.toList());

        if (affectedAppointments.isEmpty()) {
            // 如果没有患者预约，直接删除排班
            safeDeleteSchedule(schedule.getId());
            return;
        }

        // 2. 获取排班医生信息，用于查找替代方案
        Doctor originalDoctor = doctorMapper.selectById(schedule.getDoctorId());
        if (originalDoctor == null) {
            throw new RuntimeException("医生信息不存在，无法重新安排");
        }

        // 3. 为每个预约查找替代方案
        for (Appointment appointment : affectedAppointments) {
            Schedule alternativeSchedule = findAlternativeSchedule(schedule, originalDoctor);
            if (alternativeSchedule != null) {
                // 找到替代排班，重新分配预约
                reassignAppointment(appointment, alternativeSchedule, schedule);
            } else {
                // 没有找到替代方案，退款并通知
                refundAndNotifyPatient(appointment, schedule);
            }
        }

        // 4. 处理原排班关联的预支付订单：若已安排到替代排班则迁移，否则标记为待退款
        try {
            List<PrepaymentOrder> relatedOrders = prepaymentOrderMapper.selectByScheduleId(schedule.getId());
            if (relatedOrders != null && !relatedOrders.isEmpty()) {
                for (PrepaymentOrder order : relatedOrders) {
                    boolean migrated = false;
                    for (Appointment apt : affectedAppointments) {
                            if (apt.getPatientId() != null && apt.getPatientId().equals(order.getPatientId())) {
                            // 尝试找到该患者已重新分配到的新排班（appointment.scheduleId 已在 reassignAppointment 中更新）
                            prepaymentOrderMapper.updateScheduleId(order.getId(), apt.getScheduleId());
                            // 清理 waitlist_id
                            try {
                                prepaymentOrderMapper.clearWaitlistId(order.getId());
                            } catch (Exception e) {
                                logger.warn("清除waitlist_id失败，订单已迁移但保留原waitlist_id: orderId={}, error={}", order.getId(), e.getMessage());
                                // 不抛出异常，继续处理
                            }
                            migrated = true;
                                            break;
                                        }
                    }
                    if (!migrated) {
                        prepaymentOrderMapper.updateRefundInfo(order.getId(), "TO_REFUND", null, "原排班取消，订单待退款");

                        // 测试模式下立即模拟退款并通知患者
                        if (SIMULATE_REFUND) {
                            try {
                                prepaymentOrderMapper.updateRefundInfo(order.getId(), "REFUNDED", new Date(), "模拟退款：排班取消");
                                if (order.getPatientId() != null) {
                                    Patient p = patientMapper.selectById(order.getPatientId());
                                    if (p != null) {
                                        Long userId = p.getUserId();
                                        notificationService.sendAppointmentCancelledRefundNotification(
                                            userId,
                                            order.getPatientId(),
                                            null,
                                            schedule.getScheduleDate().toString(),
                                            schedule.getTimeSlot()
                                        );
                                        WaitlistWebSocketServer.pushAppointmentCancelledRefund(
                                            userId,
                                            null,
                                            schedule.getScheduleDate().toString(),
                                            schedule.getTimeSlot()
                                        );
                                    }
                                }
                            } catch (Exception ex) {
                                logger.error("模拟退款失败 orderId={}, error={}", order.getId(), ex.getMessage(), ex);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("处理预支付订单迁移/退款失败 scheduleId={}, error={}", schedule.getId(), e.getMessage(), e);
        }

        // 5. 处理该排班下的候补关联预支付订单（通过 waitlist_id 关联），并取消候补记录，避免外键约束
        try {
            List<Waitlist> waitlists = waitlistMapper.selectByScheduleId(schedule.getId());
            if (waitlists != null && !waitlists.isEmpty()) {
                for (Waitlist wl : waitlists) {
                    // 处理候补对应的预支付订单
                    List<PrepaymentOrder> wlOrders = prepaymentOrderMapper.selectByWaitlistId(wl.getId());
                    if (wlOrders != null && !wlOrders.isEmpty()) {
                        for (PrepaymentOrder order : wlOrders) {
                            boolean migrated = false;
                            for (Appointment apt : affectedAppointments) {
                                if (apt.getPatientId() != null && apt.getPatientId().equals(order.getPatientId())) {
                                    prepaymentOrderMapper.updateScheduleId(order.getId(), apt.getScheduleId());
                                    migrated = true;
                                    break;
                                }
                            }
                            if (!migrated) {
                                prepaymentOrderMapper.updateRefundInfo(order.getId(), "TO_REFUND", null, "原排班取消，候补订单待退款");
                                try {
                                    prepaymentOrderMapper.clearWaitlistId(order.getId());
                                } catch (Exception e) {
                                    logger.warn("清除waitlist_id失败，候补订单标记退款但保留原waitlist_id: orderId={}, error={}", order.getId(), e.getMessage());
                                    // 不抛出异常，继续处理
                                }
                                if (SIMULATE_REFUND) {
                                    try {
                                        prepaymentOrderMapper.updateRefundInfo(order.getId(), "REFUNDED", new Date(), "模拟退款：候补订单排班取消");
                                        if (order.getPatientId() != null) {
                                            Patient p = patientMapper.selectById(order.getPatientId());
                                            if (p != null) {
                                                notificationService.sendAppointmentCancelledRefundNotification(
                                                    p.getUserId(),
                                                    order.getPatientId(),
                                                    null,
                                                    schedule.getScheduleDate().toString(),
                                                    schedule.getTimeSlot()
                                                );
                                                WaitlistWebSocketServer.pushAppointmentCancelledRefund(
                                                    p.getUserId(),
                                                    null,
                                                    schedule.getScheduleDate().toString(),
                                                    schedule.getTimeSlot()
                                                );
                                            }
                                        }
                                    } catch (Exception ex) {
                                        logger.error("模拟退款失败（候补订单） orderId={}, error={}", order.getId(), ex.getMessage(), ex);
                                    }
                                }
                            }
                        }
                    }
                    // 标记候补记录为取消，避免残留外键引用
                    try {
                        waitlistMapper.updateStatus(wl.getId(), "CANCELLED");
                    } catch (Exception ex) {
                        logger.warn("更新候补状态失败 waitlistId={}, error={}", wl.getId(), ex.getMessage());
                    }
                                }
                            }
                        } catch (Exception e) {
            logger.error("处理候补关联预支付订单失败 scheduleId={}, error={}", schedule.getId(), e.getMessage(), e);
        }

        // 6. 删除原排班
        safeDeleteSchedule(schedule.getId());
    }

    /**
     * 按优先级查找替代排班
     * 优先级：1. 同医生+最近时间 2. 同科室+同级别医生+最近时间 3. 同科室+任何医生+最近时间
     */
    private Schedule findAlternativeSchedule(Schedule originalSchedule, Doctor originalDoctor) {
        // 1. 优先查找同一天的其他时段（更方便患者）
        Schedule alternative = findSameDayAlternative(originalSchedule, originalDoctor);
        if (alternative != null) return alternative;

        // 2. 查找同医生最近时间的排班
        alternative = findSameDoctorAlternative(originalSchedule, originalDoctor);
        if (alternative != null) return alternative;

        // 3. 查找同科室同级别医生最近时间的排班
        alternative = findSameClinicSameLevelAlternative(originalSchedule, originalDoctor);
        if (alternative != null) return alternative;

        // 4. 查找同科室任何医生最近时间的排班
        alternative = findSameClinicAnyDoctorAlternative(originalSchedule, originalDoctor);
        return alternative;
    }

    /**
     * 查找同一天其他时段的替代排班
     */
    private Schedule findSameDayAlternative(Schedule originalSchedule, Doctor doctor) {
        return scheduleMapper.findAlternativeSchedule(
            doctor.getId(),
            originalSchedule.getScheduleDate(),
            originalSchedule.getTimeSlot(),
            null, // clinicId
            null, // titleLevel
            "SAME_DAY"
        );
    }

    /**
     * 查找同医生替代排班
     */
    private Schedule findSameDoctorAlternative(Schedule originalSchedule, Doctor doctor) {
        // 查找该医生在原日期之后最近时间的排班（同一天的其他时段，或之后几天的相同或相近时段）
        return scheduleMapper.findAlternativeSchedule(
            doctor.getId(),
            originalSchedule.getScheduleDate(),
            originalSchedule.getTimeSlot(),
            null, // clinicId
            null, // titleLevel
            "SAME_DOCTOR"
        );
    }

    /**
     * 查找同科室同级别医生替代排班
     */
    private Schedule findSameClinicSameLevelAlternative(Schedule originalSchedule, Doctor doctor) {
        return scheduleMapper.findAlternativeSchedule(
            null, // doctorId
            originalSchedule.getScheduleDate(),
            originalSchedule.getTimeSlot(),
            doctor.getClinicId(),
            doctor.getTitle(),
            "SAME_CLINIC_SAME_LEVEL"
        );
    }

    /**
     * 查找同科室任何医生替代排班
     */
    private Schedule findSameClinicAnyDoctorAlternative(Schedule originalSchedule, Doctor doctor) {
        return scheduleMapper.findAlternativeSchedule(
            null, // doctorId
            originalSchedule.getScheduleDate(),
            originalSchedule.getTimeSlot(),
            doctor.getClinicId(),
            null, // titleLevel
            "SAME_CLINIC_ANY_DOCTOR"
        );
    }

    /**
     * 安全删除排班：在删除前清理所有可能的外键引用并记录详细日志
     */
    private void safeDeleteSchedule(Long scheduleId) {
        if (scheduleId == null) return;
        logger.info("safeDeleteSchedule start scheduleId={}", scheduleId);
        try {
            // 0) 先处理仍然引用该排班的预约，避免外键约束阻止删除
            try {
                Schedule sched = scheduleMapper.selectById(scheduleId);
                List<Appointment> remainingAppointments = appointmentMapper.selectByScheduleId(scheduleId);
                if (remainingAppointments != null && !remainingAppointments.isEmpty()) {
                    for (Appointment apt : remainingAppointments) {
                        try {
                            if (!"CANCELLED".equalsIgnoreCase(apt.getStatus())) {
                                logger.info("safeDeleteSchedule: 取消并通知仍引用排班的预约 appointmentId={}, scheduleId={}", apt.getId(), scheduleId);
                                refundAndNotifyPatient(apt, sched);
                            }
                        } catch (Exception ex) {
                            logger.warn("处理仍引用排班的预约失败 appointmentId={}, error={}", apt.getId(), ex.getMessage());
                            // 尽量继续处理其他预约
                        }
                    }
                }
            } catch (Exception ex) {
                logger.warn("在 safeDeleteSchedule 处理预约时发生异常（忽略） scheduleId={}, error={}", scheduleId, ex.getMessage());
            }

                // 1) 先尝试删除引用该排班候补记录的预支付订单（如果存在），因为 waitlist_id 列不允许为 NULL
                int deletedOrders = 0;
                try {
                    deletedOrders = prepaymentOrderMapper.deleteByWaitlistScheduleId(scheduleId);
                    logger.info("deleteByWaitlistScheduleId affectedRows={}, scheduleId={}", deletedOrders, scheduleId);
                } catch (Exception ex) {
                    logger.warn("deleteByWaitlistScheduleId failed scheduleId={}, error={}", scheduleId, ex.getMessage());
                    // 兜底：尝试清空 waitlist_id（若 DB 允许）
                    try {
                        int cleared = prepaymentOrderMapper.clearWaitlistIdByScheduleId(scheduleId);
                        logger.info("clearWaitlistIdByScheduleId affectedRows={}, scheduleId={}", cleared, scheduleId);
                    } catch (Exception ex2) {
                        logger.warn("clearWaitlistIdByScheduleId failed scheduleId={}, error={}", scheduleId, ex2.getMessage());
                    }
                }

            // 2) 将该排班下的候补记录统一标记为 CANCELLED（避免残留引用）
            try {
                int updated = waitlistMapper.updateStatusByScheduleId(scheduleId, "CANCELLED");
                logger.info("waitlist updateStatusByScheduleId affectedRows={}, scheduleId={}", updated, scheduleId);
            } catch (Exception ex) {
                logger.warn("waitlist updateStatusByScheduleId failed scheduleId={}, error={}", scheduleId, ex.getMessage());
            }

            // 3) 最后尝试删除排班
            try {
                scheduleMapper.deleteById(scheduleId);
                logger.info("safeDeleteSchedule completed scheduleId={}", scheduleId);
            } catch (Exception exDel) {
                // 如果仍有外键引用阻止删除（如仍有 appointment 引用），改为将排班标记为已停用（软删除式处理）
                logger.warn("无法删除排班（可能存在残留外键），改为停用排班 scheduleId={}, error={}", scheduleId, exDel.getMessage());
                try {
                    com.example.springboot.entity.Schedule mark = new com.example.springboot.entity.Schedule();
                    mark.setId(scheduleId);
                    mark.setTotalSlots(0);
                    mark.setAvailableSlots(0);
                    mark.setSlotType("CANCELLED");
                    // 通过 updateById 将其设为不可用，保留记录以避免外键问题
                    scheduleMapper.updateById(mark);
                    logger.info("safeDeleteSchedule: 已将排班标记为取消/停用 scheduleId={}", scheduleId);
                } catch (Exception exUpd) {
                    logger.error("safeDeleteSchedule: 无法停用排班 scheduleId={}, error={}", scheduleId, exUpd.getMessage(), exUpd);
                    throw exDel; // 抛原始删除异常，向上层反馈
                }
            }
        } catch (Exception e) {
            logger.error("safeDeleteSchedule failed scheduleId={}, error={}", scheduleId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 重新分配预约到新排班
     */
    private void reassignAppointment(Appointment appointment, Schedule newSchedule, Schedule originalSchedule) {
        // 1. 更新预约的排班ID和医生ID
        appointment.setScheduleId(newSchedule.getId());
        appointment.setDoctorId(newSchedule.getDoctorId());
        // 标记为系统自动重新安排，设置创建时间以作为重新选择窗口起点
        appointment.setSourceType("RESCHEDULED");
        Date now = new Date();
        appointment.setCreatedAt(now);
        // 设置重新选择窗口，默认 24 小时
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.HOUR, 24);
        appointment.setRescheduleWindowExpires(cal.getTime());
        appointment.setAutoAssigned(true);
        // 将状态设置为 RESCHEDULED，使前端能明显看到这个预约已被重新安排（并可重新选择）
        appointment.setStatus("RESCHEDULED");
        appointmentMapper.updateById(appointment);

        // 迁移或更新该患者在原排班/候补下的预支付订单到新的 schedule（如果存在）
        try {
            List<PrepaymentOrder> orders = prepaymentOrderMapper.selectByScheduleId(originalSchedule.getId());
            if (orders != null) {
                for (PrepaymentOrder order : orders) {
                    if (order.getPatientId() != null && order.getPatientId().equals(appointment.getPatientId())) {
                        prepaymentOrderMapper.updateScheduleId(order.getId(), newSchedule.getId());
                        // 清理 waitlist 引用，订单已成为正式预约关联
                        try { prepaymentOrderMapper.clearWaitlistId(order.getId()); } catch (Exception ignored) {}
                        // 记录迁移审计
                        try {
                            com.example.springboot.entity.AppointmentMigrationAudit audit = new com.example.springboot.entity.AppointmentMigrationAudit();
                            audit.setAppointmentId(appointment.getId());
                            audit.setFromScheduleId(originalSchedule.getId());
                            audit.setToScheduleId(newSchedule.getId());
                            Long operatorId = null;
                            try { operatorId = com.example.springboot.config.SecurityUtils.getCurrentUserId(); } catch (Exception ignoredOp) {}
                            audit.setOperatorId(operatorId);
                            audit.setNote("Auto reassigned by system during schedule change approval");
                            appointmentMigrationAuditMapper.insert(audit);
                        } catch (Exception auditEx) {
                            logger.warn("写入迁移审计失败 appointmentId={}, error={}", appointment.getId(), auditEx.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("迁移预支付订单到新排班失败 appointmentId={}, error={}", appointment.getId(), e.getMessage());
        }

        // 2. 扣减新排班的可用号源
        scheduleMapper.decreaseAvailableSlots(newSchedule.getId());

        // 3. 发送重新安排通知
        sendReassignmentNotification(appointment, originalSchedule, newSchedule);
        
        // 4. （已由 sendReassignmentNotification 负责发送 APPOINTMENT_RESCHEDULED IN_APP 通知和 WS 推送）
    }

    /**
     * 退款并通知患者
     */
    private void refundAndNotifyPatient(Appointment appointment, Schedule originalSchedule) {
        // 1. 更新预约状态为取消
        appointment.setStatus("CANCELLED");
        appointmentMapper.updateById(appointment);

        // 2. TODO: 处理退款逻辑（这里先标记，需要根据实际业务实现）
        // refundOrder(appointment);

        // 3. 发送退款通知
        sendRefundNotification(appointment, originalSchedule);
    }

    /**
     * 发送重新安排通知
     */
    private void sendReassignmentNotification(Appointment appointment, Schedule originalSchedule, Schedule newSchedule) {
        try {
            Patient patient = patientMapper.selectById(appointment.getPatientId());
            if (patient == null) return;

            Doctor newDoctor = doctorMapper.selectById(newSchedule.getDoctorId());
            // 格式化为友好中文显示（例如：2025年12月27日 上午）
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy年MM月dd日");
            String newDateStr = newSchedule != null && newSchedule.getScheduleDate() != null ? sdf.format(newSchedule.getScheduleDate()) : "";
            String originalDateStr = originalSchedule != null && originalSchedule.getScheduleDate() != null ? sdf.format(originalSchedule.getScheduleDate()) : "";
            String newTimeSlotCn = timeSlotToChinese(newSchedule != null ? newSchedule.getTimeSlot() : null);
            String originalTimeSlotCn = timeSlotToChinese(originalSchedule != null ? originalSchedule.getTimeSlot() : null);

            notificationService.sendAppointmentRescheduledNotification(
                patient.getUserId(),
                appointment.getPatientId(),
                appointment.getId(),
                newDoctor != null ? newDoctor.getName() : "医生",
                originalDateStr,
                originalTimeSlotCn,
                newDateStr,
                newTimeSlotCn
            );

            // WebSocket推送（payload 使用中文时间描述）
            WaitlistWebSocketServer.pushAppointmentRescheduled(
                patient.getUserId(),
                appointment.getId(),
                newDoctor != null ? newDoctor.getName() : "医生",
                newDateStr,
                newTimeSlotCn
            );

        } catch (Exception e) {
            // 记录错误但不影响业务流程
            System.err.println("发送重新安排通知失败: " + e.getMessage());
        }
    }

    /**
     * 发送改期重新安排通知
     */
    private void sendRescheduleNotification(Appointment appointment, Schedule originalSchedule, Schedule newSchedule) {
        try {
            Patient patient = patientMapper.selectById(appointment.getPatientId());
            if (patient == null) return;

            Doctor newDoctor = doctorMapper.selectById(newSchedule.getDoctorId());
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy年MM月dd日");
            String newDateStr = newSchedule != null && newSchedule.getScheduleDate() != null ? sdf.format(newSchedule.getScheduleDate()) : "";
            String originalDateStr = originalSchedule != null && originalSchedule.getScheduleDate() != null ? sdf.format(originalSchedule.getScheduleDate()) : "";
            String newTimeSlotCn = timeSlotToChinese(newSchedule != null ? newSchedule.getTimeSlot() : null);
            String originalTimeSlotCn = timeSlotToChinese(originalSchedule != null ? originalSchedule.getTimeSlot() : null);

            notificationService.sendAppointmentRescheduledNotification(
                patient.getUserId(),
                appointment.getPatientId(),
                appointment.getId(),
                newDoctor != null ? newDoctor.getName() : "医生",
                originalDateStr,
                originalTimeSlotCn,
                newDateStr,
                newTimeSlotCn
            );

            // WebSocket推送
            WaitlistWebSocketServer.pushAppointmentRescheduled(
                patient.getUserId(),
                appointment.getId(),
                newDoctor != null ? newDoctor.getName() : "医生",
                newDateStr,
                newTimeSlotCn
            );

        } catch (Exception e) {
            System.err.println("发送改期重新安排通知失败: " + e.getMessage());
        }
    }

    /**
     * 将 timeSlot 转为中文表示
     */
    private String timeSlotToChinese(String timeSlot) {
        if (timeSlot == null) return "";
        String s = timeSlot.trim().toLowerCase();
        switch (s) {
            case "morning":
            case "上午":
                return "上午";
            case "afternoon":
            case "下午":
                return "下午";
            case "evening":
            case "晚上":
                return "晚上";
            default:
                return timeSlot;
        }
    }

    /**
     * 发送改期退款通知
     */
    private void sendRescheduleRefundNotification(Appointment appointment, Schedule originalSchedule, Date newDate, String newTimeSlot) {
        try {
            Patient patient = patientMapper.selectById(appointment.getPatientId());
            if (patient == null) return;

            notificationService.sendAppointmentCancelledRefundNotification(
                patient.getUserId(),
                appointment.getPatientId(),
                appointment.getId(),
                originalSchedule.getScheduleDate().toString(),
                originalSchedule.getTimeSlot()
            );

            // WebSocket推送
            WaitlistWebSocketServer.pushAppointmentCancelledRefund(
                patient.getUserId(),
                appointment.getId(),
                originalSchedule.getScheduleDate().toString(),
                originalSchedule.getTimeSlot()
            );

        } catch (Exception e) {
            System.err.println("发送改期退款通知失败: " + e.getMessage());
        }
    }

    /**
     * 发送退款通知
     */
    private void sendRefundNotification(Appointment appointment, Schedule originalSchedule) {
        try {
            Patient patient = patientMapper.selectById(appointment.getPatientId());
            if (patient == null) return;

            notificationService.sendAppointmentCancelledRefundNotification(
                patient.getUserId(),
                appointment.getPatientId(),
                appointment.getId(),
                originalSchedule.getScheduleDate().toString(),
                originalSchedule.getTimeSlot()
            );

            // WebSocket推送
            WaitlistWebSocketServer.pushAppointmentCancelledRefund(
                patient.getUserId(),
                appointment.getId(),
                originalSchedule.getScheduleDate().toString(),
                originalSchedule.getTimeSlot()
            );

        } catch (Exception e) {
            System.err.println("发送退款通知失败: " + e.getMessage());
        }
    }

    /**
     * 处理信息修改申请批准
     */
    private void handleInfoUpdateApproval(ApplicationRequest request) {
        Doctor doctor = doctorMapper.selectById(request.getDoctorId());
        if (doctor == null) {
            throw new RuntimeException("医生不存在");
        }

        // 更新医生信息
        setDoctorFieldValue(doctor, request.getFieldName(), request.getNewValue());
        doctorMapper.update(doctor);
    }

    /**
     * 获取医生字段值
     */
    private String getDoctorFieldValue(Doctor doctor, String fieldName) {
        switch (fieldName) {
            case "name": return doctor.getName();
            case "title": return doctor.getTitle();
            case "specialty": return doctor.getSpecialty();
            case "bio": return doctor.getBio();
            default: return null;
        }
    }

    /**
     * 设置医生字段值
     */
    private void setDoctorFieldValue(Doctor doctor, String fieldName, String value) {
        switch (fieldName) {
            case "name": doctor.setName(value); break;
            case "title": doctor.setTitle(value); break;
            case "specialty": doctor.setSpecialty(value); break;
            case "bio": doctor.setBio(value); break;
            default: throw new RuntimeException("未知的字段名: " + fieldName);
        }
    }

    /**
     * 转换为详情 DTO
     */
    private ApplicationRequestDetailDTO convertToDetailDTO(ApplicationRequest request) {
        ApplicationRequestDetailDTO dto = new ApplicationRequestDetailDTO();
        dto.setId(request.getId());
        dto.setRequestType(request.getRequestType());
        dto.setRequestTypeName(getRequestTypeName(request.getRequestType()));
        
        dto.setApplicantId(request.getApplicantId());
        dto.setApplicantName(request.getApplicantName());
        if (request.getApplicant() != null) {
            dto.setApplicantUsername(request.getApplicant().getUsername());
        }
        dto.setApplicantRole(request.getApplicantRole());
        
        dto.setScheduleId(request.getScheduleId());
        dto.setChangeType(request.getChangeType());
        dto.setChangeTypeName(getChangeTypeName(request.getChangeType()));
        dto.setOriginalDate(request.getOriginalDate());
        dto.setOriginalTimeSlot(request.getOriginalTimeSlot());
        dto.setNewDate(request.getNewDate());
        dto.setNewTimeSlot(request.getNewTimeSlot());
        dto.setSlotAdjustment(request.getSlotAdjustment());
        
        dto.setDoctorId(request.getDoctorId());
        if (request.getDoctor() != null) {
            dto.setDoctorName(request.getDoctor().getName());
        }
        dto.setFieldName(request.getFieldName());
        dto.setFieldNameDisplay(getFieldNameDisplay(request.getFieldName()));
        dto.setOldValue(request.getOldValue());
        dto.setNewValue(request.getNewValue());
        
        dto.setStatus(request.getStatus());
        dto.setStatusName(getStatusName(request.getStatus()));
        dto.setReason(request.getReason());
        dto.setRejectReason(request.getRejectReason());
        
        dto.setReviewerId(request.getReviewerId());
        dto.setReviewerName(request.getReviewerName());
        if (request.getReviewer() != null) {
            dto.setReviewerUsername(request.getReviewer().getUsername());
        }
        dto.setReviewedAt(request.getReviewedAt());
        
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());
        
        return dto;
    }

    /**
     * 获取申请类型名称
     */
    private String getRequestTypeName(String requestType) {
        if (requestType == null) return "";
        switch (requestType) {
            case "SCHEDULE_CHANGE": return "调班申请";
            case "INFO_UPDATE": return "信息修改申请";
            default: return requestType;
        }
    }

    /**
     * 获取调班类型名称
     */
    private String getChangeTypeName(String changeType) {
        if (changeType == null) return "";
        switch (changeType) {
            case "CANCEL": return "取消排班";
            case "RESCHEDULE": return "改期";
            case "ADJUST_SLOTS": return "调整号源";
            default: return changeType;
        }
    }

    /**
     * 获取字段名显示
     */
    private String getFieldNameDisplay(String fieldName) {
        if (fieldName == null) return "";
        switch (fieldName) {
            case "name": return "姓名";
            case "title": return "职称";
            case "specialty": return "擅长领域";
            case "bio": return "个人简介";
            default: return fieldName;
        }
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(String status) {
        if (status == null) return "";
        switch (status) {
            case "PENDING": return "待审核";
            case "APPROVED": return "已批准";
            case "REJECTED": return "已拒绝";
            case "CANCELLED": return "已取消";
            default: return status;
        }
    }

    /**
     * 根据排班日期和时段计算预约时间
     */
    private Date calculateAppointmentTimeFromSchedule(Date scheduleDate, String timeSlot) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(scheduleDate);

        // 根据时段设置具体时间
        switch (timeSlot != null ? timeSlot.toLowerCase() : "") {
            case "morning":
                calendar.set(Calendar.HOUR_OF_DAY, 9);
                calendar.set(Calendar.MINUTE, 0);
                break;
            case "afternoon":
                calendar.set(Calendar.HOUR_OF_DAY, 14);
                calendar.set(Calendar.MINUTE, 0);
                break;
            case "evening":
                calendar.set(Calendar.HOUR_OF_DAY, 18);
                calendar.set(Calendar.MINUTE, 0);
                break;
            default:
                calendar.set(Calendar.HOUR_OF_DAY, 9);
                calendar.set(Calendar.MINUTE, 0);
                break;
        }

        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    /**
     * 获取指定日期的开始时间
     */
    private Date atStartOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 获取指定日期下一天的开始时间
     */
    private Date atStartOfNextDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 根据时间段字符串获取序号
     */
    private int timeSlotOrder(String timeSlot) {
        if (timeSlot == null) return 0;
        String s = timeSlot.trim().toLowerCase();
        if (s.equals("morning") || s.equals("上午")) return 1;
        if (s.equals("afternoon") || s.equals("下午")) return 2;
        if (s.equals("evening") || s.equals("晚上")) return 3;
        return 0;
    }

    /**
     * 根据当前小时判断当前时间段序号
     */
    private int timeSlotOrderFromNow() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return 1;
        if (hour < 18) return 2;
        return 3;
    }
}

