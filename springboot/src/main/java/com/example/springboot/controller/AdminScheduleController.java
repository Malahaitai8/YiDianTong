package com.example.springboot.controller;

import com.example.springboot.dto.*;
import com.example.springboot.entity.Schedule;
import com.example.springboot.service.ScheduleService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端排班管理控制器
 */
@RestController
@RequestMapping("/api/admin/schedules")
@CrossOrigin(origins = "*")
public class AdminScheduleController {

    @Resource
    private ScheduleService scheduleService;

    /**
     * 创建单个排班
     * POST /api/admin/schedules
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSchedule(@Valid @RequestBody CreateScheduleRequest request) {
        try {
            Schedule schedule = scheduleService.createSchedule(request);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "排班创建成功");
            response.put("data", schedule);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "创建失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 批量创建排班
     * POST /api/admin/schedules/batch
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchCreateSchedule(@Valid @RequestBody BatchScheduleRequest request) {
        try {
            Map<String, Object> result = scheduleService.batchCreateSchedule(request);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "批量创建完成");
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "批量创建失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新排班
     * PUT /api/admin/schedules/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody UpdateScheduleRequest request) {
        try {
            Schedule schedule = scheduleService.updateSchedule(id, request);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "排班更新成功");
            response.put("data", schedule);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除排班
     * DELETE /api/admin/schedules/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSchedule(@PathVariable Long id) {
        try {
            scheduleService.deleteSchedule(id);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "排班删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 根据ID查询排班详情
     * GET /api/admin/schedules/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getScheduleById(@PathVariable Long id) {
        try {
            Schedule schedule = scheduleService.getScheduleById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", schedule);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 404);
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        }
    }

    /**
     * 条件查询排班列表（带分页和筛选）
     * GET /api/admin/schedules
     * 
     * 查询参数：
     * - doctorId: 医生ID（可选）
     * - departmentId: 科室ID（可选）
     * - startDate: 开始日期（可选，格式：yyyy-MM-dd）
     * - endDate: 结束日期（可选，格式：yyyy-MM-dd）
     * - timeSlot: 时间段（可选，如：morning/afternoon/evening）
     * - slotType: 号别（可选，如：normal/expert/vip）
     * - page: 页码（默认1）
     * - pageSize: 每页大小（默认20）
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> querySchedules(ScheduleQueryRequest request) {
        try {
            Map<String, Object> result = scheduleService.querySchedules(request);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 根据医生ID查询排班列表
     * GET /api/admin/schedules/doctor/{doctorId}
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<Map<String, Object>> getSchedulesByDoctorId(@PathVariable Long doctorId) {
        try {
            List<Schedule> schedules = scheduleService.getSchedulesByDoctorId(doctorId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", schedules);
            response.put("total", schedules.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}

