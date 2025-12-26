package com.example.springboot.schedule;

import com.example.springboot.entity.Appointment;
import com.example.springboot.entity.PrepaymentOrder;
import com.example.springboot.entity.Patient;
import com.example.springboot.mapper.AppointmentMapper;
import com.example.springboot.mapper.PrepaymentOrderMapper;
import com.example.springboot.mapper.ScheduleMapper;
import com.example.springboot.mapper.PatientMapper;
import com.example.springboot.service.NotificationService;
import com.example.springboot.websocket.WaitlistWebSocketServer;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 定时任务：处理系统自动分配的预约到期确认或退款
 */
@Component
public class RescheduleFinalizer {

    private static final Logger logger = LoggerFactory.getLogger(RescheduleFinalizer.class);

    // 到期默认窗口（小时），与前端/其它位置约定一致。默认 24 小时。
    private static final int DEFAULT_WINDOW_HOURS = 24;

    @Resource
    private AppointmentMapper appointmentMapper;

    @Resource
    private PrepaymentOrderMapper prepaymentOrderMapper;

    @Resource
    private ScheduleMapper scheduleMapper;

    @Resource
    private PatientMapper patientMapper;

    @Resource
    private NotificationService notificationService;

    /**
     * 每 5 分钟扫描一次到期的自动分配预约并处理。
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    @Transactional
    public void finalizeRescheduledAppointments() {
        try {
            logger.info("RescheduleFinalizer start scanning expired auto-assigned appointments");
            int hours = DEFAULT_WINDOW_HOURS;
            List<Appointment> expired = appointmentMapper.selectRescheduledExpired(hours);
            if (expired == null || expired.isEmpty()) {
                logger.debug("RescheduleFinalizer no expired appointments found");
                return;
            }
            logger.info("RescheduleFinalizer found {} expired appointments", expired.size());

            for (Appointment appt : expired) {
                try {
                    // Double-check current status to avoid duplicate processing
                    Appointment latest = appointmentMapper.selectById(appt.getId());
                    if (latest == null) continue;
                    if (!"RESCHEDULED".equals(latest.getSourceType())) continue;

                    // Check that schedule still exists and has the appointment counted
                    if (latest.getScheduleId() == null) {
                        // cannot confirm, cancel and refund
                        cancelAndRefund(latest, "自动分配到期但无有效排班");
                        continue;
                    }

                    // Confirm final assignment: clear sourceType (or set to SCHEDULED) and keep status
                    latest.setSourceType("SCHEDULED");
                    appointmentMapper.updateById(latest);
                    logger.info("RescheduleFinalizer confirmed appointment id={} scheduleId={}", latest.getId(), latest.getScheduleId());
                    // send confirmation notification (optional) - using rescheduled template to inform finalization
                    Patient p = patientMapper.selectById(latest.getPatientId());
                    if (p != null) {
                        notificationService.sendAppointmentRescheduledNotification(
                            p.getUserId(),
                            latest.getPatientId(),
                            latest.getId(),
                            "", // doctorName not necessary here
                            "", // originalDate
                            "",
                            new java.text.SimpleDateFormat("yyyy-MM-dd").format(scheduleMapper.selectById(latest.getScheduleId()).getScheduleDate()),
                            scheduleMapper.selectById(latest.getScheduleId()).getTimeSlot()
                        );
                    }
                } catch (Exception ex) {
                    logger.error("RescheduleFinalizer process appointment id={} failed: {}", appt.getId(), ex.getMessage(), ex);
                }
            }
        } catch (Exception e) {
            logger.error("RescheduleFinalizer failed: {}", e.getMessage(), e);
        }
    }

    private void cancelAndRefund(Appointment appointment, String reason) {
        try {
            // mark appointment cancelled
            appointment.setStatus("CANCELLED");
            appointmentMapper.updateById(appointment);

            // mark related prepayment orders as TO_REFUND then REFUNDED (simulate)
            List<PrepaymentOrder> orders = prepaymentOrderMapper.selectByScheduleId(appointment.getScheduleId());
            if (orders != null) {
                for (PrepaymentOrder order : orders) {
                    if (order.getPatientId() != null && order.getPatientId().equals(appointment.getPatientId())) {
                        prepaymentOrderMapper.updateRefundInfo(order.getId(), "TO_REFUND", null, reason);
                        // simulate immediate refund for test mode
                        prepaymentOrderMapper.updateRefundInfo(order.getId(), "REFUNDED", new Date(), "RescheduleFinalizer: 模拟退款");
                    }
                }
            }

            // send refund notification
            Patient p = patientMapper.selectById(appointment.getPatientId());
            if (p != null) {
                notificationService.sendAppointmentCancelledRefundNotification(
                    p.getUserId(),
                    appointment.getPatientId(),
                    appointment.getId(),
                    appointment.getAppointmentTime() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(appointment.getAppointmentTime()) : "",
                    ""
                );
                WaitlistWebSocketServer.pushAppointmentCancelledRefund(
                    p.getUserId(),
                    appointment.getId(),
                    appointment.getAppointmentTime() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(appointment.getAppointmentTime()) : "",
                    ""
                );
            }
        } catch (Exception e) {
            logger.error("cancelAndRefund failed for appointment id={} : {}", appointment.getId(), e.getMessage(), e);
        }
    }
}


