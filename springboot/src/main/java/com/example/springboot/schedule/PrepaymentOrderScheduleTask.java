package com.example.springboot.schedule;

import com.example.springboot.service.PrepaymentOrderService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 预支付订单定时任务
 * 处理过期订单和需要退款的订单
 */
@Component
public class PrepaymentOrderScheduleTask {
    private static final Logger logger = LoggerFactory.getLogger(PrepaymentOrderScheduleTask.class);

    @Resource
    private PrepaymentOrderService prepaymentOrderService;

    /**
     * 每5分钟处理一次过期订单
     * 将状态为PENDING且已过期的订单状态更新为EXPIRED
     */
    @Scheduled(fixedRate = 300000) // 5分钟 = 300000毫秒
    public void processExpiredOrders() {
        try {
            logger.info("开始处理过期订单");
            prepaymentOrderService.processExpiredOrders();
            logger.info("过期订单处理完成");
        } catch (Exception e) {
            logger.error("处理过期订单失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 每10分钟处理一次需要退款的订单
     * 将状态为PAID且对应候补记录状态为EXPIRED或CANCELLED的订单进行退款
     */
    @Scheduled(fixedRate = 600000) // 10分钟 = 600000毫秒
    public void processRefundOrders() {
        try {
            logger.info("开始处理需要退款的订单");
            prepaymentOrderService.processRefundOrders();
            logger.info("退款订单处理完成");
        } catch (Exception e) {
            logger.error("处理退款订单失败: {}", e.getMessage(), e);
        }
    }
}
