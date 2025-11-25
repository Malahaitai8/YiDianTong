package com.example.springboot.mapper;

import com.example.springboot.entity.PrepaymentOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PrepaymentOrderMapper {
    
    /**
     * 查询所有预支付订单
     */
    List<PrepaymentOrder> selectAll();
    
    /**
     * 根据ID查询预支付订单
     */
    PrepaymentOrder selectById(Long id);
    
    /**
     * 根据订单号查询预支付订单
     */
    PrepaymentOrder selectByOrderNo(String orderNo);
    
    /**
     * 根据候补记录ID查询预支付订单
     */
    PrepaymentOrder selectByWaitlistId(Long waitlistId);
    
    /**
     * 根据患者ID查询预支付订单列表
     */
    List<PrepaymentOrder> selectByPatientId(Long patientId);
    
    /**
     * 根据患者ID和状态查询预支付订单列表
     */
    List<PrepaymentOrder> selectByPatientIdAndStatus(@Param("patientId") Long patientId, @Param("status") String status);
    
    /**
     * 插入预支付订单
     */
    int insert(PrepaymentOrder prepaymentOrder);
    
    /**
     * 更新预支付订单
     */
    int update(PrepaymentOrder prepaymentOrder);
    
    /**
     * 更新订单状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    /**
     * 更新支付信息
     */
    int updatePaymentInfo(@Param("id") Long id, 
                         @Param("status") String status,
                         @Param("paymentMethod") String paymentMethod,
                         @Param("paymentTime") java.util.Date paymentTime,
                         @Param("paidAmount") java.math.BigDecimal paidAmount);
    
    /**
     * 更新退款信息
     */
    int updateRefundInfo(@Param("id") Long id,
                        @Param("status") String status,
                        @Param("refundTime") java.util.Date refundTime,
                        @Param("refundReason") String refundReason);
    
    /**
     * 删除预支付订单
     */
    int delete(Long id);
    
    /**
     * 查询过期的订单
     */
    List<PrepaymentOrder> selectExpiredOrders();
    
    /**
     * 查询需要退款的订单（候补失败、超时等）
     */
    List<PrepaymentOrder> selectOrdersForRefund();
}
