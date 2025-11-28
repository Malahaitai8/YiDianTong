package com.example.springboot.service;

import com.example.springboot.entity.Notification;
import com.example.springboot.entity.Patient;
import com.example.springboot.entity.WechatSubscribe;
import com.example.springboot.mapper.NotificationMapper;
import com.example.springboot.mapper.PatientMapper;
import com.example.springboot.mapper.WechatSubscribeMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息通知服务
 */
@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Resource
    private NotificationMapper notificationMapper;
    
    @Resource
    private WechatSubscribeMapper wechatSubscribeMapper;
    
    @Resource
    private PatientMapper patientMapper;
    
    @Resource
    private WechatService wechatService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 微信订阅消息模板ID配置（需要在微信公众平台配置）
    private static final String TEMPLATE_WAITLIST_SUCCESS = "WAITLIST_SUCCESS_TEMPLATE_ID";
    private static final String TEMPLATE_APPOINTMENT_SUCCESS = "APPOINTMENT_SUCCESS_TEMPLATE_ID";
    private static final String TEMPLATE_APPOINTMENT_REMINDER = "APPOINTMENT_REMINDER_TEMPLATE_ID";
    private static final String TEMPLATE_APPOINTMENT_CANCEL = "APPOINTMENT_CANCEL_TEMPLATE_ID";
    
    /**
     * 发送候补成功通知
     */
    @Async
    public void sendWaitlistSuccessNotification(Long userId, Long patientId, Long appointmentId, 
                                                 String doctorName, String appointmentDate, String timeSlot) {
        try {
            Patient patient = patientMapper.selectById(patientId);
            if (patient == null) {
                logger.warn("患者不存在，无法发送通知: patientId={}", patientId);
                return;
            }
            
            // 创建通知记录
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setPatientId(patientId);
            notification.setNotificationType("WAITLIST_SUCCESS");
            notification.setTitle("候补成功");
            notification.setContent(String.format("您的候补已成功转为预约！医生：%s，时间：%s %s", 
                doctorName, appointmentDate, timeSlot));
            notification.setRelatedType("APPOINTMENT");
            notification.setRelatedId(appointmentId);
            notification.setChannel("WECHAT");
            notification.setStatus("PENDING");
            notification.setTemplateId(TEMPLATE_WAITLIST_SUCCESS);
            
            // 构建模板数据
            Map<String, Map<String, String>> templateData = wechatService.buildMessageData(
                "thing1", doctorName,
                "date2", appointmentDate,
                "time3", timeSlot,
                "thing4", "候补成功，请按时就诊"
            );
            notification.setTemplateData(objectMapper.writeValueAsString(templateData));
            
            notificationMapper.insert(notification);
            
            // 发送微信订阅消息
            sendWechatNotification(notification, patient);
            
        } catch (Exception e) {
            logger.error("发送候补成功通知失败: userId={}, patientId={}, error={}", 
                userId, patientId, e.getMessage(), e);
        }
    }
    
    /**
     * 发送预约成功通知
     */
    @Async
    public void sendAppointmentSuccessNotification(Long userId, Long patientId, Long appointmentId,
                                                    String doctorName, String appointmentDate, String timeSlot) {
        try {
            Patient patient = patientMapper.selectById(patientId);
            if (patient == null) {
                logger.warn("患者不存在，无法发送通知: patientId={}", patientId);
                return;
            }
            
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setPatientId(patientId);
            notification.setNotificationType("APPOINTMENT_SUCCESS");
            notification.setTitle("预约成功");
            notification.setContent(String.format("您已成功预约！医生：%s，时间：%s %s", 
                doctorName, appointmentDate, timeSlot));
            notification.setRelatedType("APPOINTMENT");
            notification.setRelatedId(appointmentId);
            notification.setChannel("WECHAT");
            notification.setStatus("PENDING");
            notification.setTemplateId(TEMPLATE_APPOINTMENT_SUCCESS);
            
            Map<String, Map<String, String>> templateData = wechatService.buildMessageData(
                "thing1", doctorName,
                "date2", appointmentDate,
                "time3", timeSlot,
                "thing4", "预约成功，请按时就诊"
            );
            notification.setTemplateData(objectMapper.writeValueAsString(templateData));
            
            notificationMapper.insert(notification);
            
            sendWechatNotification(notification, patient);
            
        } catch (Exception e) {
            logger.error("发送预约成功通知失败: userId={}, patientId={}, error={}", 
                userId, patientId, e.getMessage(), e);
        }
    }
    
    /**
     * 发送就诊提醒通知
     */
    @Async
    public void sendAppointmentReminderNotification(Long userId, Long patientId, Long appointmentId,
                                                     String doctorName, String appointmentDate, String timeSlot,
                                                     int hoursBefore) {
        try {
            Patient patient = patientMapper.selectById(patientId);
            if (patient == null) {
                return;
            }
            
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setPatientId(patientId);
            notification.setNotificationType("APPOINTMENT_REMINDER");
            notification.setTitle("就诊提醒");
            notification.setContent(String.format("您有预约即将就诊！医生：%s，时间：%s %s，距离就诊还有%d小时", 
                doctorName, appointmentDate, timeSlot, hoursBefore));
            notification.setRelatedType("APPOINTMENT");
            notification.setRelatedId(appointmentId);
            notification.setChannel("WECHAT");
            notification.setStatus("PENDING");
            notification.setTemplateId(TEMPLATE_APPOINTMENT_REMINDER);
            
            Map<String, Map<String, String>> templateData = wechatService.buildMessageData(
                "thing1", doctorName,
                "date2", appointmentDate,
                "time3", timeSlot,
                "thing4", String.format("距离就诊还有%d小时，请提前做好准备", hoursBefore)
            );
            notification.setTemplateData(objectMapper.writeValueAsString(templateData));
            
            notificationMapper.insert(notification);
            
            sendWechatNotification(notification, patient);
            
        } catch (Exception e) {
            logger.error("发送就诊提醒通知失败: userId={}, patientId={}, error={}", 
                userId, patientId, e.getMessage(), e);
        }
    }
    
    /**
     * 发送取消预约通知
     */
    @Async
    public void sendAppointmentCancelNotification(Long userId, Long patientId, Long appointmentId,
                                                  String doctorName, String appointmentDate) {
        try {
            Patient patient = patientMapper.selectById(patientId);
            if (patient == null) {
                return;
            }
            
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setPatientId(patientId);
            notification.setNotificationType("APPOINTMENT_CANCEL");
            notification.setTitle("预约已取消");
            notification.setContent(String.format("您的预约已取消。医生：%s，原定时间：%s", 
                doctorName, appointmentDate));
            notification.setRelatedType("APPOINTMENT");
            notification.setRelatedId(appointmentId);
            notification.setChannel("WECHAT");
            notification.setStatus("PENDING");
            notification.setTemplateId(TEMPLATE_APPOINTMENT_CANCEL);
            
            Map<String, Map<String, String>> templateData = wechatService.buildMessageData(
                "thing1", doctorName,
                "date2", appointmentDate,
                "thing3", "预约已取消，退款将原路退回"
            );
            notification.setTemplateData(objectMapper.writeValueAsString(templateData));
            
            notificationMapper.insert(notification);
            
            sendWechatNotification(notification, patient);
            
        } catch (Exception e) {
            logger.error("发送取消预约通知失败: userId={}, patientId={}, error={}", 
                userId, patientId, e.getMessage(), e);
        }
    }
    
    /**
     * 发送微信订阅消息
     */
    private void sendWechatNotification(Notification notification, Patient patient) {
        try {
            // 获取用户的微信OpenID（需要从用户信息中获取，这里假设从patient表或其他地方获取）
            // 实际实现中，需要在用户登录时保存OpenID
            String openid = getOpenidByUserId(notification.getUserId());
            if (openid == null || openid.isEmpty()) {
                logger.warn("用户未绑定微信OpenID，跳过发送: userId={}", notification.getUserId());
                notificationMapper.updateStatus(notification.getId(), "FAILED", new Date(), 
                    "用户未绑定微信OpenID");
                return;
            }
            
            // 检查授权状态
            WechatSubscribe subscribe = wechatSubscribeMapper.selectValidAuth(
                notification.getUserId(), 
                notification.getTemplateId(), 
                new Date()
            );
            
            if (subscribe == null || !subscribe.getAuthorized()) {
                logger.warn("用户未授权订阅消息，跳过发送: userId={}, templateId={}", 
                    notification.getUserId(), notification.getTemplateId());
                notificationMapper.updateStatus(notification.getId(), "FAILED", new Date(), 
                    "用户未授权订阅消息");
                return;
            }
            
            // 解析模板数据
            Map<String, Map<String, String>> templateData = objectMapper.readValue(
                notification.getTemplateData(), 
                objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Map.class)
            );
            
            // 构建跳转页面路径
            String page = buildPagePath(notification);
            
            // 发送订阅消息
            boolean success = wechatService.sendSubscribeMessage(
                openid, 
                notification.getTemplateId(), 
                page, 
                templateData
            );
            
            // 更新通知状态
            if (success) {
                notificationMapper.updateStatus(notification.getId(), "SENT", new Date(), null);
            } else {
                notificationMapper.updateStatus(notification.getId(), "FAILED", new Date(), 
                    "微信API返回失败");
            }
            
        } catch (Exception e) {
            logger.error("发送微信通知异常: notificationId={}, error={}", 
                notification.getId(), e.getMessage(), e);
            try {
                notificationMapper.updateStatus(notification.getId(), "FAILED", new Date(), 
                    e.getMessage());
            } catch (Exception ex) {
                logger.error("更新通知状态失败: {}", ex.getMessage());
            }
        }
    }
    
    /**
     * 根据用户ID获取OpenID（需要根据实际业务实现）
     */
    private String getOpenidByUserId(Long userId) {
        // 从微信订阅授权表中获取OpenID
        try {
            List<WechatSubscribe> subscribes = wechatSubscribeMapper.selectByUserId(userId);
            if (subscribes != null && !subscribes.isEmpty()) {
                // 返回第一个有效的OpenID
                return subscribes.get(0).getOpenid();
            }
        } catch (Exception e) {
            logger.warn("获取用户OpenID失败: userId={}, error={}", userId, e.getMessage());
        }
        return null;
    }
    
    /**
     * 构建跳转页面路径
     */
    private String buildPagePath(Notification notification) {
        if ("APPOINTMENT".equals(notification.getRelatedType()) && notification.getRelatedId() != null) {
            return String.format("pages/appointment-detail/appointment-detail?id=%d", notification.getRelatedId());
        } else if ("WAITLIST_SUCCESS".equals(notification.getNotificationType())) {
            return "pages/records/records?tab=appointment";
        }
        return "pages/index/index";
    }
    
    /**
     * 查询用户通知列表
     */
    public List<Notification> getUserNotifications(Long userId, Integer limit) {
        return notificationMapper.selectByUserId(userId, limit);
    }
    
    /**
     * 查询未读通知数量
     */
    public Long getUnreadCount(Long userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }
    
    /**
     * 标记为已读
     */
    public void markAsRead(Long notificationId) {
        notificationMapper.markAsRead(notificationId, new Date());
    }
    
    /**
     * 全部标记为已读
     */
    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId, new Date());
    }
}

