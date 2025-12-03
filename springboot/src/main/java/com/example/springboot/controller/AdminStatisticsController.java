package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Tag(name = "管理端统计报表", description = "提供全局统计数据报表")
@RestController
@RequestMapping("/api/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearer-jwt")
public class AdminStatisticsController {

    @Resource
    private StatisticsService statisticsService;

    @Operation(summary = "获取全局概览统计", description = "提供全站的核心运营指标，如预约量、完成率、号源利用率等")
    @GetMapping("/overview")
    public Result getOverviewStats() {
        try {
            return Result.success(statisticsService.getOverviewStats());
        } catch (Exception e) {
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取预约统计", description = "按日期范围统计预约情况，支持按日期、科室、医生等维度统计")
    @GetMapping("/appointments")
    public Result getAppointmentStatistics(
            @Parameter(description = "开始日期，格式：yyyy-MM-dd") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            // 如果没有指定日期范围，默认查询最近30天
            if (startDate == null || endDate == null) {
                endDate = new Date();
                startDate = new Date(endDate.getTime() - 30L * 24 * 60 * 60 * 1000);
            }
            return Result.success(statisticsService.getAppointmentStatistics(startDate, endDate));
        } catch (Exception e) {
            return Result.error("获取预约统计失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取科室负荷统计", description = "统计各科室的预约量、完成率、退号率、号源利用率等")
    @GetMapping("/departments/workload")
    public Result getDepartmentWorkload(
            @Parameter(description = "开始日期，格式：yyyy-MM-dd") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            if (startDate == null || endDate == null) {
                endDate = new Date();
                startDate = new Date(endDate.getTime() - 30L * 24 * 60 * 60 * 1000);
            }
            return Result.success(statisticsService.getDepartmentWorkload(startDate, endDate));
        } catch (Exception e) {
            return Result.error("获取科室负荷统计失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取医生工作量统计", description = "统计各医生的工作天数、接诊患者数、日均接诊数、收入等")
    @GetMapping("/doctors/workload")
    public Result getDoctorWorkload(
            @Parameter(description = "开始日期，格式：yyyy-MM-dd") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @Parameter(description = "医生ID，可选") @RequestParam(required = false) Long doctorId) {
        try {
            if (startDate == null || endDate == null) {
                endDate = new Date();
                startDate = new Date(endDate.getTime() - 30L * 24 * 60 * 60 * 1000);
            }
            return Result.success(statisticsService.getDoctorWorkload(startDate, endDate, doctorId));
        } catch (Exception e) {
            return Result.error("获取医生工作量统计失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取收入统计", description = "按日期范围统计收入情况")
    @GetMapping("/revenue")
    public Result getRevenueStatistics(
            @Parameter(description = "开始日期，格式：yyyy-MM-dd") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            if (startDate == null || endDate == null) {
                endDate = new Date();
                startDate = new Date(endDate.getTime() - 30L * 24 * 60 * 60 * 1000);
            }
            return Result.success(statisticsService.getRevenueStatistics(startDate, endDate));
        } catch (Exception e) {
            return Result.error("获取收入统计失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取收入统计（按科室）", description = "按科室统计收入情况")
    @GetMapping("/revenue/departments")
    public Result getRevenueByDepartment(
            @Parameter(description = "开始日期，格式：yyyy-MM-dd") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            if (startDate == null || endDate == null) {
                endDate = new Date();
                startDate = new Date(endDate.getTime() - 30L * 24 * 60 * 60 * 1000);
            }
            return Result.success(statisticsService.getRevenueByDepartment(startDate, endDate));
        } catch (Exception e) {
            return Result.error("获取科室收入统计失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取号别分布统计", description = "统计不同号别（普通、专家、特需）的预约分布")
    @GetMapping("/slot-type-distribution")
    public Result getSlotTypeDistribution(
            @Parameter(description = "开始日期，格式：yyyy-MM-dd") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            if (startDate == null || endDate == null) {
                endDate = new Date();
                startDate = new Date(endDate.getTime() - 30L * 24 * 60 * 60 * 1000);
            }
            return Result.success(statisticsService.getSlotTypeDistribution(startDate, endDate));
        } catch (Exception e) {
            return Result.error("获取号别分布统计失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取时间段分布统计", description = "统计不同时间段（上午、下午）的预约分布")
    @GetMapping("/time-slot-distribution")
    public Result getTimeSlotDistribution(
            @Parameter(description = "开始日期，格式：yyyy-MM-dd") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            if (startDate == null || endDate == null) {
                endDate = new Date();
                startDate = new Date(endDate.getTime() - 30L * 24 * 60 * 60 * 1000);
            }
            return Result.success(statisticsService.getTimeSlotDistribution(startDate, endDate));
        } catch (Exception e) {
            return Result.error("获取时间段分布统计失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取退号率统计（按科室）", description = "统计各科室的退号率和退号数量")
    @GetMapping("/cancellation-rate/departments")
    public Result getCancellationRateByDepartment(
            @Parameter(description = "开始日期，格式：yyyy-MM-dd") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            if (startDate == null || endDate == null) {
                endDate = new Date();
                startDate = new Date(endDate.getTime() - 30L * 24 * 60 * 60 * 1000);
            }
            return Result.success(statisticsService.getCancellationRateByDepartment(startDate, endDate));
        } catch (Exception e) {
            return Result.error("获取科室退号率统计失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取退号率统计（按医生）", description = "统计各医生的退号率和退号数量")
    @GetMapping("/cancellation-rate/doctors")
    public Result getCancellationRateByDoctor(
            @Parameter(description = "开始日期，格式：yyyy-MM-dd") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @Parameter(description = "医生ID，可选") @RequestParam(required = false) Long doctorId) {
        try {
            if (startDate == null || endDate == null) {
                endDate = new Date();
                startDate = new Date(endDate.getTime() - 30L * 24 * 60 * 60 * 1000);
            }
            return Result.success(statisticsService.getCancellationRateByDoctor(startDate, endDate, doctorId));
        } catch (Exception e) {
            return Result.error("获取医生退号率统计失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取趋势分析", description = "获取预约、收入、退号的趋势数据，用于图表展示")
    @GetMapping("/trends")
    public Result getTrendStatistics(
            @Parameter(description = "开始日期，格式：yyyy-MM-dd") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            if (startDate == null || endDate == null) {
                endDate = new Date();
                startDate = new Date(endDate.getTime() - 30L * 24 * 60 * 60 * 1000);
            }
            return Result.success(statisticsService.getTrendStatistics(startDate, endDate));
        } catch (Exception e) {
            return Result.error("获取趋势统计失败: " + e.getMessage());
        }
    }
}

