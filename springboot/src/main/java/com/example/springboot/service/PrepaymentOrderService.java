package com.example.springboot.service;

import com.example.springboot.entity.Patient;
import com.example.springboot.entity.PrepaymentOrder;
import com.example.springboot.entity.Schedule;
import com.example.springboot.entity.Waitlist;
import com.example.springboot.exception.CustomerException;
import com.example.springboot.mapper.PatientMapper;
import com.example.springboot.mapper.PrepaymentOrderMapper;
import com.example.springboot.mapper.ScheduleMapper;
import com.example.springboot.mapper.WaitlistMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Calendar;

/**
 * 候补预支付订单服务
 */
@Service
public class PrepaymentOrderService {
    private static final Logger logger = LoggerFactory.getLogger(PrepaymentOrderService.class);

    @Resource
    private PrepaymentOrderMapper prepaymentOrderMapper;

    @Resource
    private WaitlistMapper waitlistMapper;

    @Resource
    private PatientMapper patientMapper;

    @Resource
    private ScheduleMapper scheduleMapper;

    @Resource
    private SystemConfigService systemConfigService;

    /**
     * 创建候补预支付订单
     * @param patientId 患者ID
     * @param scheduleId 排班ID
     * @param waitlistId 候补记录ID
     * @return 创建的预支付订单
     */
    @Transactional
    public PrepaymentOrder createWaitlistPrepayment(Long patientId, Long scheduleId, Long waitlistId) {
        Waitlist waitlist = resolveOrCreateWaitlist(patientId, scheduleId, waitlistId);
        Long resolvedWaitlistId = waitlist.getId();
        
        // 4. 检查是否已经存在预支付订单
        PrepaymentOrder existingOrder = prepaymentOrderMapper.selectOneByWaitlistId(resolvedWaitlistId);
        if (existingOrder != null) {
            String status = existingOrder.getStatus();
            // 如果订单已过期、已退款或已取消，删除旧订单，创建新订单
            if ("EXPIRED".equals(status) || "REFUNDED".equals(status) || "CANCELLED".equals(status)) {
                prepaymentOrderMapper.delete(existingOrder.getId());
                logger.info("删除旧订单，创建新订单: orderNo={}, oldStatus={}", existingOrder.getOrderNo(), status);
            } else if ("PENDING".equals(status) || "PAID".equals(status)) {
                // 订单状态正常，直接返回已存在的订单
                return existingOrder;
            } else {
                // 其他状态（如 CONSUMED），也删除旧订单，创建新订单
                prepaymentOrderMapper.delete(existingOrder.getId());
                logger.info("删除旧订单（状态异常），创建新订单: orderNo={}, oldStatus={}", existingOrder.getOrderNo(), status);
            }
        }
        
        // 5. 获取排班信息，计算费用
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new CustomerException("排班信息不存在");
        }
        
        // 6. 计算原始费用和实际费用
        BigDecimal originalFee = calculateOriginalFee(schedule.getSlotType());
        BigDecimal actualFee = calculateActualFee(patientId, originalFee);
        
        // 7. 生成订单号
        String orderNo = generateOrderNo();
        
        // 8. 设置过期时间（默认30分钟）
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 30);
        Date expireTime = calendar.getTime();
        
        // 9. 创建预支付订单
        PrepaymentOrder order = new PrepaymentOrder();
        order.setOrderNo(orderNo);
        order.setPatientId(patientId);
        order.setScheduleId(scheduleId);
        order.setWaitlistId(resolvedWaitlistId);
        order.setOrderType("WAITLIST");
        order.setOriginalFee(originalFee);
        order.setActualFee(actualFee);
        order.setPaidAmount(BigDecimal.ZERO);
        order.setStatus("PENDING");
        order.setExpireTime(expireTime);
        order.setCreatedAt(new Date());
        order.setUpdatedAt(new Date());
        
        // 10. 保存订单
        prepaymentOrderMapper.insert(order);
        
        return order;
    }
    
    /**
     * 支付预支付订单
     * @param orderNo 订单号
     * @param paymentMethod 支付方式
     * @param paidAmount 支付金额
     * @return 更新后的订单
     */
    @Transactional
    public PrepaymentOrder payOrder(String orderNo, String paymentMethod, BigDecimal paidAmount) {
        // 1. 查询订单
        PrepaymentOrder order = prepaymentOrderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new CustomerException("订单不存在");
        }
        
        // 2. 检查订单状态
        if (!"PENDING".equals(order.getStatus())) {
            throw new CustomerException("订单状态不正确，无法支付");
        }
        
        // 3. 检查订单是否过期
        if (order.getExpireTime().before(new Date())) {
            order.setStatus("EXPIRED");
            prepaymentOrderMapper.updateStatus(order.getId(), "EXPIRED");
            throw new CustomerException("订单已过期，请重新创建");
        }
        
        // 4. 检查支付金额
        if (paidAmount.compareTo(order.getActualFee()) < 0) {
            throw new CustomerException("支付金额不足");
        }
        
        // 5. 更新订单状态和支付信息
        prepaymentOrderMapper.updatePaymentInfo(
            order.getId(),
            "PAID",
            paymentMethod,
            new Date(),
            paidAmount
        );

        waitlistMapper.updateStatus(order.getWaitlistId(), "NOTIFIED");
        
        // 6. 重新查询并返回更新后的订单
        return prepaymentOrderMapper.selectById(order.getId());
    }
    
    /**
     * 退款
     * @param orderNo 订单号
     * @param reason 退款原因
     * @return 更新后的订单
     */
    @Transactional
    public PrepaymentOrder refundOrder(String orderNo, String reason) {
        // 1. 查询订单
        PrepaymentOrder order = prepaymentOrderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new CustomerException("订单不存在");
        }
        
        // 2. 检查订单状态
        if (!"PAID".equals(order.getStatus())) {
            throw new CustomerException("订单状态不正确，无法退款");
        }
        
        // 3. 更新订单状态和退款信息
        prepaymentOrderMapper.updateRefundInfo(
            order.getId(),
            "REFUNDED",
            new Date(),
            reason
        );
        
        // 4. 重新查询并返回更新后的订单
        return prepaymentOrderMapper.selectById(order.getId());
    }
    
    /**
     * 处理候补成功，消费预支付订单
     * @param waitlistId 候补记录ID
     * @return 更新后的订单
     */
    @Transactional
    public PrepaymentOrder consumeOrder(Long waitlistId) {
        // 1. 查询订单
        PrepaymentOrder order = prepaymentOrderMapper.selectOneByWaitlistId(waitlistId);
        if (order == null) {
            throw new CustomerException("预支付订单不存在");
        }
        
        // 2. 检查订单状态
        if (!"PAID".equals(order.getStatus())) {
            throw new CustomerException("订单状态不正确，无法消费");
        }
        
        // 3. 更新订单状态
        prepaymentOrderMapper.updateStatus(order.getId(), "CONSUMED");
        
        // 4. 重新查询并返回更新后的订单
        return prepaymentOrderMapper.selectById(order.getId());
    }
    
    /**
     * 处理过期订单
     */
    @Transactional
    public void processExpiredOrders() {
        List<PrepaymentOrder> expiredOrders = prepaymentOrderMapper.selectExpiredOrders();
        for (PrepaymentOrder order : expiredOrders) {
            prepaymentOrderMapper.updateStatus(order.getId(), "EXPIRED");
            try {
                waitlistMapper.updateStatus(order.getWaitlistId(), "EXPIRED");
            } catch (Exception ignored) {}
            logger.info("订单已过期: {}", order.getOrderNo());
        }
    }
    
    /**
     * 处理需要退款的订单
     */
    @Transactional
    public void processRefundOrders() {
        List<PrepaymentOrder> refundOrders = prepaymentOrderMapper.selectOrdersForRefund();
        for (PrepaymentOrder order : refundOrders) {
            try {
                refundOrder(order.getOrderNo(), "候补失败或取消，系统自动退款");
                logger.info("订单已退款: {}", order.getOrderNo());
            } catch (Exception e) {
                logger.error("订单退款失败: {}, 原因: {}", order.getOrderNo(), e.getMessage());
            }
        }
    }
    
    /**
     * 根据排班类型计算原始费用
     */
    private BigDecimal calculateOriginalFee(String slotType) {
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
        return systemConfigService.getDecimalOrDefault(key, new BigDecimal("0.00"));
    }
    
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
                // 学生报销95%，实付5%
                return originalFee.multiply(new BigDecimal("0.05"));
            } else if ("teacher".equals(specificRole)) {
                // 教师报销90%，实付10%
                return originalFee.multiply(new BigDecimal("0.10"));
            }
            
            return originalFee;
        } catch (Exception e) {
            logger.warn("计算实际费用失败: {}", e.getMessage());
            return originalFee;
        }
    }
    
    /**
     * 根据患者ID查询预支付订单列表
     */
    public List<PrepaymentOrder> getOrdersByPatientId(Long patientId) {
        return prepaymentOrderMapper.selectByPatientId(patientId);
    }

    /**
     * 根据订单号查询预支付订单
     */
    public PrepaymentOrder getOrderByNo(String orderNo) {
        return prepaymentOrderMapper.selectByOrderNo(orderNo);
    }

    /**
     * 查询所有预支付订单
     */
    public List<PrepaymentOrder> getAllOrders() {
        return prepaymentOrderMapper.selectAll();
    }

    /**
     * 根据候补记录ID查询预支付订单
     */
    public PrepaymentOrder getOrderByWaitlistId(Long waitlistId) {
        return prepaymentOrderMapper.selectOneByWaitlistId(waitlistId);
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        // 生成格式: WL + 时间戳 + 4位随机数
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.format("%04d", (int)(Math.random() * 10000));
        return "WL" + timestamp + random;
    }

    private Waitlist resolveOrCreateWaitlist(Long patientId, Long scheduleId, Long waitlistId) {
        Waitlist waitlist;
        if (waitlistId != null) {
            waitlist = waitlistMapper.selectById(waitlistId);
            if (waitlist == null) {
                throw new CustomerException("候补记录不存在，请刷新后重试");
            }
            if (!waitlist.getPatientId().equals(patientId)) {
                throw new CustomerException("候补记录与患者不匹配");
            }
            if (!waitlist.getScheduleId().equals(scheduleId)) {
                throw new CustomerException("候补记录与排班不匹配");
            }
        } else {
            waitlist = waitlistMapper.selectByScheduleAndPatient(scheduleId, patientId);
            if (waitlist == null) {
                // 首次候补，创建一条记录（先标记为 NOTIFIED，支付完成后才能进入 WAITING）
                waitlist = new Waitlist();
                waitlist.setPatientId(patientId);
                waitlist.setScheduleId(scheduleId);
                waitlist.setStatus("NOTIFIED");
                waitlist.setJoinTime(new Date());
                waitlistMapper.insert(waitlist);
            } else {
                String status = waitlist.getStatus() == null ? "WAITING" : waitlist.getStatus().toUpperCase();
                if ("WAITING".equals(status)) {
                    throw new CustomerException("已在候补队列中，请勿重复提交");
                }
                if ("GRANTED".equals(status)) {
                    throw new CustomerException("候补已成功，请在预约记录中查看结果");
                }
                // 其他状态（NOTIFIED/EXPIRED/CANCELLED）允许重新走预支付流程
            }
        }

        // 统一将状态切换为 NOTIFIED，表示已完成预支付，等待正式加入队列
        waitlist.setStatus("NOTIFIED");
        waitlist.setJoinTime(new Date());
        waitlistMapper.update(waitlist);
        return waitlist;
    }
}
