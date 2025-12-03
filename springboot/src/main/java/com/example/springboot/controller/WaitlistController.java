package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.dto.CreateWaitlistRequest;
import com.example.springboot.dto.WaitlistInfoDTO;
import com.example.springboot.entity.Patient;
import com.example.springboot.entity.PrepaymentOrder;
import com.example.springboot.entity.Waitlist;
import com.example.springboot.mapper.PatientMapper;
import com.example.springboot.mapper.PrepaymentOrderMapper;
import com.example.springboot.mapper.WaitlistMapper;
import com.example.springboot.service.PrepaymentOrderService;
import com.example.springboot.service.WaitlistService;
import jakarta.annotation.Resource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;

@Tag(name = "候补队列", description = "候补队列相关接口")
@RestController
@RequestMapping("/waitlist")
@SecurityRequirement(name = "bearer-jwt")
public class WaitlistController {

    @Resource
    private WaitlistService waitlistService;

    @Resource
    private PatientMapper patientMapper;

    @Resource
    private WaitlistMapper waitlistMapper;

    @Resource
    private PrepaymentOrderService prepaymentOrderService;

    @Resource
    private PrepaymentOrderMapper prepaymentOrderMapper;


    /** [修改] 加入候补队列 */
    @Operation(summary = "加入候补队列", description = "当号源已满时，患者可加入候补队列")
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public Result addToQueue(@jakarta.validation.Valid @RequestBody CreateWaitlistRequest request) {
        Long userId = com.example.springboot.config.SecurityUtils.getCurrentUserId();
        Patient patient = patientMapper.selectByUserId(userId);
        if (patient == null) {
            return Result.error("当前用户不是有效的患者");
        }
        
        // 检查是否已经存在该患者对该排班的候补记录
        Waitlist existingWaitlist = waitlistMapper.selectByScheduleAndPatient(request.getScheduleId(), patient.getId());
        if (existingWaitlist != null) {
            // 如果已存在候补记录，直接使用现有的
            if (!"WAITING".equals(existingWaitlist.getStatus()) && !"GRANTED".equals(existingWaitlist.getStatus())) {
                // 如果不是等待中或已获得预约的状态，则更新状态
                existingWaitlist.setJoinTime(new Date());
                existingWaitlist.setStatus("WAITING");
                waitlistMapper.update(existingWaitlist);
                
                // 加入队列
                waitlistService.addToQueue(existingWaitlist);
                return Result.success("已重新加入候补队列");
            } else {
                return Result.error("已在候补队列中，请勿重复提交");
            }
        }
        
        // 创建新的候补记录
        Waitlist waitlist = new Waitlist();
        waitlist.setScheduleId(request.getScheduleId());
        waitlist.setPatientId(patient.getId());
        waitlist.setJoinTime(new Date());
        waitlist.setStatus("WAITING");
        
        // 插入数据库
        waitlistMapper.insert(waitlist);
        
        // 加入Redis队列
        waitlistService.addToQueue(waitlist);
        
        return Result.success("加入候补成功");
    }

    /** [修改] 查看我的候补列表 */
    @Operation(summary = "查看我的候补", description = "查看当前登录患者的候补记录 (包含排名)")
    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public Result myQueue() {
        Long userId = com.example.springboot.config.SecurityUtils.getCurrentUserId();
        Patient patient = patientMapper.selectByUserId(userId);
        if (patient == null) {
            // 如果不是有效患者，返回空列表是合理的，不应报错
            return Result.success(java.util.Collections.emptyList());
        }
        List<WaitlistInfoDTO> myQueues = waitlistService.listByPatient(patient.getId());
        return Result.success(myQueues);
    }

    /** [修改] 弹出队首（管理员或系统任务） */
    @Operation(summary = "弹出队首(手动)", description = "管理员手动弹出队首，返回弹出的患者ID (不创建预约)")
    @PostMapping("/next/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result popNext(@PathVariable Long scheduleId) {
        Waitlist waitlist = waitlistService.popNext(scheduleId);
        if (waitlist == null) {
            return Result.error("队列为空");
        }
        return Result.success(waitlist.getPatientId());
    }

    @Operation(summary = "查询多个排班的候补人数", description = "返回每个排班的候补队列数量，需管理员权限")
    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public Result getQueueSizes(@RequestParam("scheduleIds") List<Long> scheduleIds) {
        return Result.success(waitlistService.getQueueSizes(scheduleIds));
    }

    /** 退出候补队列（患者） */
    @Operation(summary = "退出候补队列", description = "患者从指定排班的候补队列中移除自身，同步删除候补记录和预支付订单")
    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasRole('PATIENT')")
    public Result cancel(@PathVariable Long scheduleId) {
        Long userId = com.example.springboot.config.SecurityUtils.getCurrentUserId();
        Patient patient = patientMapper.selectByUserId(userId);
        if (patient == null) {
            return Result.error("当前用户不是有效的患者");
        }
        Waitlist waitlist = waitlistMapper.selectByScheduleAndPatient(scheduleId, patient.getId());
        if (waitlist == null) {
            return Result.error("未找到候补记录");
        }
        
        Long waitlistId = waitlist.getId();
        
        // 1. 从 Redis 队列中移除候补记录
        try {
            waitlistService.removeFromQueue(waitlist);
        } catch (Exception e) {
            // 忽略队列移除失败的错误，继续执行删除操作
            System.out.println("从队列移除候补记录失败: " + e.getMessage());
        }

        // 2. 处理预支付订单
        PrepaymentOrder order = prepaymentOrderService.getOrderByWaitlistId(waitlistId);
        String orderStatus = null;
        if (order != null) {
            orderStatus = order.getStatus();
            if ("PAID".equals(orderStatus)) {
                // 已支付的订单，需要退款
                try {
                    prepaymentOrderService.refundOrder(order.getOrderNo(), "用户取消候补，自动退款");
                    orderStatus = "REFUNDED";
                } catch (Exception e) {
                    // 退款失败，仍然删除订单
                    System.out.println("退款失败: " + e.getMessage());
                }
            }
            // 删除预支付订单（无论状态如何）
            try {
                prepaymentOrderMapper.delete(order.getId());
            } catch (Exception e) {
                System.out.println("删除预支付订单失败: " + e.getMessage());
            }
        }

        // 3. 删除候补记录
        try {
            waitlistMapper.delete(waitlistId);
        } catch (Exception e) {
            return Result.error("删除候补记录失败: " + e.getMessage());
        }

        // 4. 返回结果
        if ("REFUNDED".equals(orderStatus)) {
            return Result.success("已取消候补并原路退回预支付费用");
        } else if (order != null) {
            return Result.success("已取消候补并删除预支付订单");
        } else {
            return Result.success("已退出候补队列");
        }
    }
}