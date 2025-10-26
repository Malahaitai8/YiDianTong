package com.example.springboot.controller;

import com.example.springboot.dto.ApprovalRequest;
import com.example.springboot.dto.PendingDoctorDTO;
import com.example.springboot.entity.Doctor;
import com.example.springboot.entity.User;
import com.example.springboot.mapper.DoctorMapper;
import com.example.springboot.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 医生审核接口
 * 用于管理员审核医生注册申请
 */
@RestController
@RequestMapping("/api/admin/approval")
@Tag(name = "医生注册审核", description = "管理员对医生注册账号的审核接口")
@SecurityRequirement(name = "bearer-jwt")
public class DoctorApprovalController {

    @Resource
    private UserService userService;

    @Resource
    private DoctorMapper doctorMapper;

    /**
     * 获取所有待审核的医生列表
     * @return 待审核医生列表
     */
    @GetMapping("/doctors/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取待审核医生列表", description = "返回状态为pending_approval的医生用户及其资料")
    public ResponseEntity<Map<String, Object>> getPendingDoctors() {
        Map<String, Object> response = new HashMap<>();
        try {
            // 查询所有状态为 pending_approval 的医生用户
            List<User> pendingUsers = userService.getPendingApprovalUsers("DOCTOR");
            List<PendingDoctorDTO> pendingDoctors = new ArrayList<>();

            for (User user : pendingUsers) {
                // 查询对应的医生信息
                Doctor doctor = doctorMapper.selectByUserId(user.getId());
                if (doctor != null) {
                    PendingDoctorDTO dto = new PendingDoctorDTO();
                    dto.setUserId(user.getId());
                    dto.setUsername(user.getUsername());
                    dto.setDoctorId(doctor.getId());
                    dto.setName(doctor.getName());
                    dto.setTitle(doctor.getTitle());
                    dto.setSpecialty(doctor.getSpecialty());
                    dto.setBio(doctor.getBio());
                    dto.setClinicId(doctor.getClinicId());
                    if (doctor.getClinic() != null) {
                        dto.setClinicName(doctor.getClinic().getName());
                    }
                    dto.setCreatedAt(user.getCreatedAt());
                    dto.setStatus(user.getStatus());
                    pendingDoctors.add(dto);
                }
            }

            response.put("success", true);
            response.put("data", pendingDoctors);
            response.put("total", pendingDoctors.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取待审核医生列表失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 审核通过 - 批准医生注册
     * @param request 审核请求
     * @return 审核结果
     */
    @PostMapping("/doctors/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "审核通过医生注册",
            description = "将指定用户的状态从pending_approval更新为active",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = ApprovalRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "审核通过"),
                    @ApiResponse(responseCode = "400", description = "参数错误"),
                    @ApiResponse(responseCode = "500", description = "服务器错误")
            }
    )
    public ResponseEntity<Map<String, Object>> approveDoctor(@RequestBody ApprovalRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (request.getUserId() == null) {
                response.put("success", false);
                response.put("message", "用户ID不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 检查用户是否存在
            User user = userService.getById(request.getUserId());
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.badRequest().body(response);
            }

            // 检查用户是否为待审核状态
            if (!"pending_approval".equals(user.getStatus())) {
                response.put("success", false);
                response.put("message", "该用户不是待审核状态");
                return ResponseEntity.badRequest().body(response);
            }

            // 更新用户状态为 active
            int result = userService.approveUser(request.getUserId());
            if (result > 0) {
                response.put("success", true);
                response.put("message", "审核通过，医生账号已激活");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "审核操作失败");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "审核操作失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 审核拒绝 - 拒绝医生注册
     * @param request 审核请求
     * @return 审核结果
     */
    @PostMapping("/doctors/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "审核拒绝医生注册",
            description = "将指定用户的状态从pending_approval更新为inactive，并可记录拒绝原因",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = ApprovalRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "已拒绝"),
                    @ApiResponse(responseCode = "400", description = "参数或状态错误"),
                    @ApiResponse(responseCode = "500", description = "服务器错误")
            }
    )
    public ResponseEntity<Map<String, Object>> rejectDoctor(@RequestBody ApprovalRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (request.getUserId() == null) {
                response.put("success", false);
                response.put("message", "用户ID不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 检查用户是否存在
            User user = userService.getById(request.getUserId());
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.badRequest().body(response);
            }

            // 检查用户是否为待审核状态
            if (!"pending_approval".equals(user.getStatus())) {
                response.put("success", false);
                response.put("message", "该用户不是待审核状态");
                return ResponseEntity.badRequest().body(response);
            }

            // 更新用户状态为 inactive
            int result = userService.rejectUser(request.getUserId());
            if (result > 0) {
                response.put("success", true);
                response.put("message", "已拒绝该医生的注册申请");
                if (request.getReason() != null && !request.getReason().isEmpty()) {
                    response.put("reason", request.getReason());
                }
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "审核操作失败");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "审核操作失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 批量审核通过
     * @param userIds 用户ID列表
     * @return 审核结果
     */
    @PostMapping("/doctors/approve/batch")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "批量审核通过医生注册",
            description = "批量将多个用户从pending_approval更新为active",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = List.class))
            )
    )
    public ResponseEntity<Map<String, Object>> batchApprove(@RequestBody List<Long> userIds) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userIds == null || userIds.isEmpty()) {
                response.put("success", false);
                response.put("message", "用户ID列表不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            int successCount = 0;
            int failCount = 0;
            List<String> errors = new ArrayList<>();

            for (Long userId : userIds) {
                try {
                    User user = userService.getById(userId);
                    if (user != null && "pending_approval".equals(user.getStatus())) {
                        int result = userService.approveUser(userId);
                        if (result > 0) {
                            successCount++;
                        } else {
                            failCount++;
                            errors.add("用户ID " + userId + " 更新失败");
                        }
                    } else {
                        failCount++;
                        errors.add("用户ID " + userId + " 不存在或不是待审核状态");
                    }
                } catch (Exception e) {
                    failCount++;
                    errors.add("用户ID " + userId + " 处理异常: " + e.getMessage());
                }
            }

            response.put("success", true);
            response.put("message", String.format("批量审核完成: 成功 %d 个, 失败 %d 个", successCount, failCount));
            response.put("successCount", successCount);
            response.put("failCount", failCount);
            if (!errors.isEmpty()) {
                response.put("errors", errors);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "批量审核操作失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取待审核医生详情
     * @param userId 用户ID
     * @return 医生详情
     */
    @GetMapping("/doctors/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取待审核医生详情", description = "根据用户ID获取医生的注册资料")
    public ResponseEntity<Map<String, Object>> getDoctorDetail(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userService.getById(userId);
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.badRequest().body(response);
            }

            Doctor doctor = doctorMapper.selectByUserId(userId);
            if (doctor == null) {
                response.put("success", false);
                response.put("message", "医生信息不存在");
                return ResponseEntity.badRequest().body(response);
            }

            PendingDoctorDTO dto = new PendingDoctorDTO();
            dto.setUserId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setDoctorId(doctor.getId());
            dto.setName(doctor.getName());
            dto.setTitle(doctor.getTitle());
            dto.setSpecialty(doctor.getSpecialty());
            dto.setBio(doctor.getBio());
            dto.setClinicId(doctor.getClinicId());
            if (doctor.getClinic() != null) {
                dto.setClinicName(doctor.getClinic().getName());
            }
            dto.setCreatedAt(user.getCreatedAt());
            dto.setStatus(user.getStatus());

            response.put("success", true);
            response.put("data", dto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取医生详情失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

