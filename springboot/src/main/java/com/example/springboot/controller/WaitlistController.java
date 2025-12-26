package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.dto.CreateWaitlistRequest;
import com.example.springboot.dto.WaitlistInfoDTO;
import com.example.springboot.entity.Patient;
import com.example.springboot.entity.PrepaymentOrder;
import com.example.springboot.entity.Waitlist;
import com.example.springboot.mapper.PatientMapper;
import com.example.springboot.mapper.WaitlistMapper;
import com.example.springboot.service.PrepaymentOrderService;
import com.example.springboot.service.WaitlistService;
import jakarta.annotation.Resource;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Map;
import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
// [删除] import org.springframework.web.bind.annotation.PutMapping; // 未使用
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
        Waitlist waitlist = waitlistMapper.selectById(request.getWaitlistId());
        if (waitlist == null) {
            return Result.error("候补记录不存在");
        }
        if (!waitlist.getPatientId().equals(patient.getId())) {
            return Result.error("候补记录与患者不匹配");
        }
        if (!waitlist.getScheduleId().equals(request.getScheduleId())) {
            return Result.error("候补记录与排班不匹配");
        }

        String currentStatus = waitlist.getStatus();
        if (currentStatus != null) {
            switch (currentStatus) {
                case "WAITING":
                    return Result.error("已在候补队列中，请勿重复提交");
                case "GRANTED":
                    return Result.error("候补已成功，无需重复加入");
                case "EXPIRED":
                    return Result.error("候补记录已失效，请重新发起");
                default:
                    break;
            }

        }

        PrepaymentOrder order = prepaymentOrderService.getOrderByWaitlistId(waitlist.getId());
        if (order == null || !"PAID".equals(order.getStatus())) {
            return Result.error("请先完成预支付费用");
        }

        waitlist.setStatus("WAITING");
        waitlist.setJoinTime(new Date());
        waitlistMapper.update(waitlist);

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

    /** 退出候补队列（患者） */
    @Operation(summary = "退出候补队列", description = "患者从指定排班的候补队列中移除自身")
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
        try {
            waitlistService.removeFromQueue(waitlist);
        } catch (Exception ignored) {}

        waitlist.setStatus("EXPIRED");
        waitlistMapper.update(waitlist);

        PrepaymentOrder order = prepaymentOrderService.getOrderByWaitlistId(waitlist.getId());
        if (order != null && "PAID".equals(order.getStatus())) {
            try {
                prepaymentOrderService.refundOrder(order.getOrderNo(), "用户取消候补，自动退款");
                return Result.success("已取消候补并原路退回预支付费用");
            } catch (Exception e) {
                return Result.error("候补取消成功，但退款失败：" + e.getMessage());
            }
        }

        return Result.success("已退出候补队列");
    }

    /**
     * 查询候补队列人数（公开接口，所有登录用户可访问）
     */
    @Operation(summary = "查询候补队列人数", description = "查询指定排班的候补队列人数")
    @PostMapping("/count")
    @PreAuthorize("isAuthenticated()") // 只需要登录即可
    public Result getQueueCounts(@RequestBody Map<String, List<Long>> request) {
        List<Long> scheduleIds = request.get("scheduleIds");
        if (scheduleIds == null || scheduleIds.isEmpty()) {
            return Result.error("scheduleIds不能为空");
        }

        try {
            Map<Long, Long> counts = waitlistService.getQueueSizes(scheduleIds);
            return Result.success(counts);
        } catch (Exception e) {
            return Result.error("查询候补人数失败: " + e.getMessage());
        }
    }
}