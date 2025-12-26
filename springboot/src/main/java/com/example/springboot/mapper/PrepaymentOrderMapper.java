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
    List<PrepaymentOrder> selectByWaitlistId(Long waitlistId);
    
    /**
     * 查询单条预支付订单（根据候补记录ID）
     */
    PrepaymentOrder selectOneByWaitlistId(Long waitlistId);
    
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

    /**
     * 查询指定排班ID关联的预支付订单
     */
    List<PrepaymentOrder> selectByScheduleId(@Param("scheduleId") Long scheduleId);

    /**
     * 将预支付订单绑定到新的排班（用于改期迁移）
     */
    int updateScheduleId(@Param("id") Long id, @Param("scheduleId") Long scheduleId);

    /**
     * 将预支付订单的 waitlist_id 置空（用于删除候补记录前清理外键）
     */
    int clearWaitlistId(@Param("id") Long id);
    
    /**
     * 清理指定排班下所有预支付订单的 waitlist_id 引用（用于删除候补/排班前清理外键）
     */
    int clearWaitlistIdByScheduleId(@Param("scheduleId") Long scheduleId);
    
    /**
     * 删除引用指定排班下候补记录的预支付订单（用于删除候补/排班前彻底移除引用）
     */
    int deleteByWaitlistScheduleId(@Param("scheduleId") Long scheduleId);
}
