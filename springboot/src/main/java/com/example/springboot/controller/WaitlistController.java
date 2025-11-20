package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.dto.CreateWaitlistRequest;
import com.example.springboot.dto.WaitlistInfoDTO; // <-- [新增] 导入
// [删除] import com.example.springboot.entity.Waitlist; // 不再需要
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
// [删除] import org.springframework.web.bind.annotation.PutMapping; // 未使用
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List; // <-- [新增] 导入

@Tag(name = "候补队列", description = "候补队列相关接口")
@RestController
@RequestMapping("/waitlist")
@SecurityRequirement(name = "bearer-jwt")
public class WaitlistController {

    @Resource
    private WaitlistService waitlistService;


    /** [修改] 加入候补队列 */
    @Operation(summary = "加入候补队列", description = "当号源已满时，患者可加入候补队列")
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public Result addToQueue(@jakarta.validation.Valid @RequestBody CreateWaitlistRequest request) {
        Long patientId = com.example.springboot.config.SecurityUtils.getCurrentUserId();

        // [修改] Service 已包含校验逻辑 (号源是否已满, 是否重复)
        // [修改] Service 方法现在返回 void
        waitlistService.addToQueue(patientId, request.getScheduleId());

        return Result.success("加入候补成功"); // [修改] 返回成功信息
    }

    /** [修改] 查看我的候补列表 */
    @Operation(summary = "查看我的候补", description = "查看当前登录患者的候补记录 (包含排名)")
    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public Result myQueue() {
        Long patientId = com.example.springboot.config.SecurityUtils.getCurrentUserId();

        // [修改] service 现在返回 List<WaitlistInfoDTO>
        List<WaitlistInfoDTO> myQueues = waitlistService.listByPatient(patientId);

        return Result.success(myQueues);
    }

    /** [修改] 弹出队首（管理员或系统任务） */
    @Operation(summary = "弹出队首(手动)", description = "管理员手动弹出队首，返回弹出的患者ID (不创建预约)")
    @PostMapping("/next/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result popNext(@PathVariable Long scheduleId) {

        // [修改] service 现在返回 Long patientId
        Long patientId = waitlistService.popNext(scheduleId);

        if (patientId == null) {
            return Result.error("队列为空");
        }
        return Result.success(patientId);
    }

    /** [新增] 退出候补队列 */
    @Operation(summary = "退出候补队列", description = "患者主动退出候补队列")
    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasRole('PATIENT')")
    public Result cancel(@PathVariable Long scheduleId) {
        Long patientId = com.example.springboot.config.SecurityUtils.getCurrentUserId();
        waitlistService.removeFromQueue(patientId, scheduleId);
        return Result.success("退出成功");
    }
}