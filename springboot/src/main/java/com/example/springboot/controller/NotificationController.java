package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.config.SecurityUtils;
import com.example.springboot.entity.Notification;
import com.example.springboot.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "消息通知", description = "患者端消息通知接口")
@RestController
@RequestMapping("/notifications")
@SecurityRequirement(name = "bearer-jwt")
public class NotificationController {

    @Resource
    private NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "查询我的通知列表")
    public Result list(@RequestParam(value = "limit", required = false) Integer limit) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }
        List<Notification> notifications = notificationService.getUserNotifications(userId, limit);
        return Result.success(notifications);
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "查询未读通知数量")
    public Result unreadCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }
        Long count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "标记单条通知为已读")
    public Result markRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success("已标记为已读");
    }

    @PostMapping("/read-all")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "全部标记为已读")
    public Result markAllRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }
        notificationService.markAllAsRead(userId);
        return Result.success("全部已读");
    }
}


































