package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.CreateWaitlistPrepaymentRequest;
import com.example.springboot.dto.PayPrepaymentOrderRequest;
import com.example.springboot.dto.RefundPrepaymentOrderRequest;
import com.example.springboot.entity.Patient;
import com.example.springboot.entity.PrepaymentOrder;
import com.example.springboot.mapper.PatientMapper;
import com.example.springboot.service.PrepaymentOrderService;
import com.example.springboot.config.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "候补预支付管理", description = "候补预支付订单相关接口")
@RestController
@RequestMapping("/prepayment")
public class PrepaymentOrderController {

    @Resource
    private PrepaymentOrderService prepaymentOrderService;

    @Resource
    private PatientMapper patientMapper;

    @Operation(summary = "创建候补预支付订单", description = "为候补记录创建预支付订单，预扣费用")
    @SecurityRequirement(name = "bearer-jwt")
    @PostMapping("/waitlist")
    @PreAuthorize("hasRole('PATIENT')")
    public Result createWaitlistPrepayment(@jakarta.validation.Valid @RequestBody CreateWaitlistPrepaymentRequest request) {
        try {
            // 获取当前用户的患者ID
            Long userId = SecurityUtils.getCurrentUserId();
            Patient patient = patientMapper.selectByUserId(userId);
            if (patient == null) {
                return Result.error("患者信息不存在，请先完善个人信息");
            }

            PrepaymentOrder order = prepaymentOrderService.createWaitlistPrepayment(
                patient.getId(),
                request.getScheduleId(),
                request.getWaitlistId()
            );

            return Result.success(order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "支付预支付订单", description = "完成预支付订单的支付")
    @SecurityRequirement(name = "bearer-jwt")
    @PostMapping("/pay")
    @PreAuthorize("hasRole('PATIENT')")
    public Result payOrder(@jakarta.validation.Valid @RequestBody PayPrepaymentOrderRequest request) {
        try {
            PrepaymentOrder order = prepaymentOrderService.payOrder(
                request.getOrderNo(),
                request.getPaymentMethod(),
                request.getPaidAmount()
            );

            return Result.success(order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "申请退款", description = "主动退出候补时申请退款")
    @SecurityRequirement(name = "bearer-jwt")
    @PostMapping("/refund")
    @PreAuthorize("hasRole('PATIENT')")
    public Result refundOrder(@jakarta.validation.Valid @RequestBody RefundPrepaymentOrderRequest request) {
        try {
            PrepaymentOrder order = prepaymentOrderService.refundOrder(
                request.getOrderNo(),
                request.getReason() != null ? request.getReason() : "用户主动申请退款"
            );

            return Result.success(order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "查询我的预支付订单", description = "查询当前患者的所有预支付订单")
    @SecurityRequirement(name = "bearer-jwt")
    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public Result getMyOrders() {
        try {
            // 获取当前用户的患者ID
            Long userId = SecurityUtils.getCurrentUserId();
            Patient patient = patientMapper.selectByUserId(userId);
            if (patient == null) {
                return Result.error("患者信息不存在");
            }

            List<PrepaymentOrder> orders = prepaymentOrderService.getOrdersByPatientId(patient.getId());
            return Result.success(orders);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "根据订单号查询订单", description = "根据订单号查询预支付订单详情")
    @SecurityRequirement(name = "bearer-jwt")
    @GetMapping("/order/{orderNo}")
    @PreAuthorize("hasRole('PATIENT')")
    public Result getOrderByNo(@Parameter(description = "订单号") @PathVariable String orderNo) {
        try {
            PrepaymentOrder order = prepaymentOrderService.getOrderByNo(orderNo);
            if (order == null) {
                return Result.error("订单不存在");
            }

            // 验证订单是否属于当前用户
            Long userId = SecurityUtils.getCurrentUserId();
            Patient patient = patientMapper.selectByUserId(userId);
            if (patient == null || !order.getPatientId().equals(patient.getId())) {
                return Result.error("无权限查看此订单");
            }

            return Result.success(order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "管理员查询所有订单", description = "管理员查询所有预支付订单")
    @SecurityRequirement(name = "bearer-jwt")
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public Result getAllOrders() {
        try {
            List<PrepaymentOrder> orders = prepaymentOrderService.getAllOrders();
            return Result.success(orders);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "管理员处理过期订单", description = "管理员手动处理过期的预支付订单")
    @SecurityRequirement(name = "bearer-jwt")
    @PostMapping("/admin/process-expired")
    @PreAuthorize("hasRole('ADMIN')")
    public Result processExpiredOrders() {
        try {
            prepaymentOrderService.processExpiredOrders();
            return Result.success("过期订单处理完成");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "管理员处理退款订单", description = "管理员手动处理需要退款的订单")
    @SecurityRequirement(name = "bearer-jwt")
    @PostMapping("/admin/process-refund")
    @PreAuthorize("hasRole('ADMIN')")
    public Result processRefundOrders() {
        try {
            prepaymentOrderService.processRefundOrders();
            return Result.success("退款订单处理完成");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
