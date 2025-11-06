package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.config.SecurityUtils;
import com.example.springboot.dto.*;
import com.example.springboot.entity.User;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.service.ApplicationRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 申请记录控制器
 */
@Tag(name = "申请管理", description = "调班申请和医生信息修改申请的管理接口")
@RestController
@RequestMapping("/api/application-requests")
@SecurityRequirement(name = "bearer-jwt")
public class ApplicationRequestController {

    @Resource
    private ApplicationRequestService applicationRequestService;

    @Resource
    private UserMapper userMapper;

    /**
     * 创建申请（医生或管理员）
     */
    @Operation(summary = "创建申请", description = "医生或管理员创建调班申请或信息修改申请")
    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public Result createRequest(@Valid @RequestBody CreateApplicationRequestDTO dto) {
        try {
            String username = SecurityUtils.getCurrentUsername();
            User user = userMapper.selectByUsername(username);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            ApplicationRequestDetailDTO request = applicationRequestService.createRequest(
                    dto, user.getId(), user.getRole());
            return Result.success(request);
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 查询所有申请（管理员）
     */
    @Operation(summary = "查询所有申请", description = "管理员查询所有申请记录")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result getAllRequests() {
        try {
            List<ApplicationRequestDetailDTO> requests = applicationRequestService.getAllRequests();
            return Result.success(requests);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询我的申请（医生或管理员）
     */
    @Operation(summary = "查询我的申请", description = "查询当前用户提交的所有申请")
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public Result getMyRequests() {
        try {
            String username = SecurityUtils.getCurrentUsername();
            User user = userMapper.selectByUsername(username);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            List<ApplicationRequestDetailDTO> requests = 
                    applicationRequestService.getRequestsByApplicant(user.getId());
            return Result.success(requests);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询申请
     */
    @Operation(summary = "查询申请详情", description = "根据ID查询单个申请的详细信息")
    @GetMapping("/{requestId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public Result getRequestById(
            @Parameter(description = "申请ID", required = true) @PathVariable Long requestId) {
        try {
            ApplicationRequestDetailDTO request = applicationRequestService.getRequestById(requestId);
            return Result.success(request);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据ID查询申请详情（直接从数据库联查，包含完整关联信息）
     */
    @Operation(summary = "查询申请完整详情", description = "根据ID查询包含申请人、审核人、医生等完整关联信息的详情")
    @GetMapping("/detail/{requestId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public Result getRequestDetailById(
            @Parameter(description = "申请ID", required = true) @PathVariable Long requestId) {
        try {
            ApplicationRequestDetailDTO detail = applicationRequestService.getRequestDetailById(requestId);
            return Result.success(detail);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据状态查询申请（管理员）
     */
    @Operation(summary = "按状态查询申请", description = "管理员根据状态查询申请")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result getRequestsByStatus(
            @Parameter(description = "状态：PENDING、APPROVED、REJECTED、CANCELLED", required = true) 
            @PathVariable String status) {
        try {
            List<ApplicationRequestDetailDTO> requests = applicationRequestService.getRequestsByStatus(status);
            return Result.success(requests);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询待审核的申请（管理员）
     */
    @Operation(summary = "查询待审核申请", description = "管理员查询所有待审核的申请")
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public Result getPendingRequests() {
        try {
            List<ApplicationRequestDetailDTO> requests = applicationRequestService.getPendingRequests();
            return Result.success(requests);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 审核申请（管理员）
     */
    @Operation(summary = "审核申请", description = "管理员批准或拒绝申请")
    @PostMapping("/review")
    @PreAuthorize("hasRole('ADMIN')")
    public Result reviewRequest(@Valid @RequestBody ReviewApplicationRequestDTO dto) {
        try {
            String username = SecurityUtils.getCurrentUsername();
            User user = userMapper.selectByUsername(username);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            ApplicationRequestDetailDTO request = applicationRequestService.reviewRequest(dto, user.getId());
            return Result.success(request);
        } catch (Exception e) {
            return Result.error("审核失败: " + e.getMessage());
        }
    }

    /**
     * 取消申请（申请人）
     */
    @Operation(summary = "取消申请", description = "申请人取消自己的待审核申请")
    @PostMapping("/{requestId}/cancel")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public Result cancelRequest(
            @Parameter(description = "申请ID", required = true) @PathVariable Long requestId) {
        try {
            String username = SecurityUtils.getCurrentUsername();
            User user = userMapper.selectByUsername(username);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            applicationRequestService.cancelRequest(requestId, user.getId());
            return Result.success("申请已取消");
        } catch (Exception e) {
            return Result.error("取消失败: " + e.getMessage());
        }
    }

    /**
     * 删除申请（管理员）
     */
    @Operation(summary = "删除申请", description = "管理员删除申请记录（谨慎操作）")
    @DeleteMapping("/{requestId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result deleteRequest(
            @Parameter(description = "申请ID", required = true) @PathVariable Long requestId) {
        try {
            applicationRequestService.deleteRequest(requestId);
            return Result.success("申请已删除");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取申请统计（管理员）
     */
    @Operation(summary = "获取申请统计", description = "管理员查看申请统计数据")
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public Result getStatistics() {
        try {
            Map<String, Integer> stats = applicationRequestService.getRequestStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}

