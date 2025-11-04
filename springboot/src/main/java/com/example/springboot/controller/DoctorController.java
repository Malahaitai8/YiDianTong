package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.config.SecurityUtils;
import com.example.springboot.dto.ApplicationRequestDetailDTO;
import com.example.springboot.dto.CreateApplicationRequestDTO;
import com.example.springboot.entity.Doctor;
import com.example.springboot.entity.User;
import com.example.springboot.mapper.DoctorMapper;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.service.ApplicationRequestService;
import com.example.springboot.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize; // <-- [新增] 导入
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Tag(name = "医生管理", description = "医生信息管理相关接口")
@RestController
@RequestMapping("/doctor")
@SecurityRequirement(name = "bearer-jwt")
public class DoctorController {

    @Resource
    private DoctorService doctorService;

    @Resource
    private DoctorMapper doctorMapper;
    
    @Resource
    private ApplicationRequestService applicationRequestService;
    
    @Resource
    private UserMapper userMapper;


    @Operation(summary = "查询所有医生", description = "获取系统中所有医生信息")
    @GetMapping("/selectAll")
    @PreAuthorize("isAuthenticated()") // <-- [新增] 任何登录用户
    public Result selectAll() {
        List<Doctor> list = doctorService.selectAll();
        return Result.success(list);
    }

    @Operation(summary = "根据ID查询医生", description = "通过医生ID获取医生详情")
    @GetMapping("/selectById/{id}")
    @PreAuthorize("isAuthenticated()") // <-- [新增] 任何登录用户
    public Result selectById(
            @Parameter(description = "医生ID", required = true) @PathVariable Long id) {
        Doctor doctor = doctorService.selectById(id);
        return Result.success(doctor);
    }

    @Operation(summary = "新增医生", description = "仅管理员可创建医生记录")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // <-- [新增] 仅管理员
    public Result create(@jakarta.validation.Valid @RequestBody Doctor doctor) {
        doctorService.create(doctor);
        return Result.success();
    }

    @Operation(summary = "更新医生", description = "管理员或医生本人可更新医生信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public Result update(
            @Parameter(description = "医生ID", required = true) @PathVariable Long id,
            @jakarta.validation.Valid @RequestBody Doctor doctor) {
        doctor.setId(id); // 使用路径参数中的ID
        doctorService.update(doctor); // Service层已实现本人权限校验
        return Result.success();
    }

    @Operation(summary = "删除医生", description = "仅管理员可删除医生记录")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // <-- [新增] 仅管理员
    public Result delete(
            @Parameter(description = "医生ID", required = true) @PathVariable Long id) {
        doctorService.delete(id);
        return Result.success();
    }

    // ==================== 医生端专属功能 ====================

    /**
     * 医生查看自己的排班
     * GET /doctor/my-schedules
     */
    @Operation(summary = "查看我的排班", description = "医生查看自己的排班记录，支持日期范围筛选")
    @GetMapping("/my-schedules")
    @PreAuthorize("hasRole('DOCTOR')")
    public Result getMySchedules(
            @Parameter(description = "开始日期（格式：yyyy-MM-dd）") 
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            
            @Parameter(description = "结束日期（格式：yyyy-MM-dd）") 
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            
            @Parameter(description = "时间段（MORNING/AFTERNOON/EVENING）") 
            @RequestParam(required = false) String timeSlot) {
        
        try {
            // 获取当前医生ID
            Long userId = SecurityUtils.getCurrentUserId();
            Doctor doctor = doctorMapper.selectByUserId(userId);
            if (doctor == null) {
                return Result.error("当前用户不是医生");
            }
            
            // 查询排班
            Map<String, Object> result = doctorService.getMySchedules(
                doctor.getId(), startDate, endDate, timeSlot
            );
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 医生查看预约患者列表
     * GET /doctor/my-patients
     */
    @Operation(summary = "查看预约患者列表", description = "医生查看自己名下的预约患者清单")
    @GetMapping("/my-patients")
    @PreAuthorize("hasRole('DOCTOR')")
    public Result getMyPatients(
            @Parameter(description = "日期（格式：yyyy-MM-dd），不传则查询所有") 
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
            
            @Parameter(description = "时间段（MORNING/AFTERNOON/EVENING）") 
            @RequestParam(required = false) String timeSlot,
            
            @Parameter(description = "预约状态（PENDING/CONFIRMED/COMPLETED/CANCELLED）") 
            @RequestParam(required = false) String status,
            
            @Parameter(description = "患者姓名（模糊搜索）") 
            @RequestParam(required = false) String patientName) {
        
        try {
            // 获取当前医生ID
            Long userId = SecurityUtils.getCurrentUserId();
            Doctor doctor = doctorMapper.selectByUserId(userId);
            if (doctor == null) {
                return Result.error("当前用户不是医生");
            }
            
            // 查询患者列表
            Map<String, Object> result = doctorService.getMyPatients(
                doctor.getId(), date, timeSlot, status, patientName
            );
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 医生个人Dashboard
     * GET /doctor/dashboard
     */
    @Operation(summary = "医生个人Dashboard", description = "展示医生的统计数据和关键信息")
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('DOCTOR')")
    public Result getDashboard() {
        try {
            // 获取当前医生ID
            Long userId = SecurityUtils.getCurrentUserId();
            Doctor doctor = doctorMapper.selectByUserId(userId);
            if (doctor == null) {
                return Result.error("当前用户不是医生");
            }
            
            // 获取Dashboard数据
            Map<String, Object> dashboard = doctorService.getDoctorDashboard(doctor.getId());
            
            return Result.success(dashboard);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    // ==================== 个人信息修改申请 ====================
    
    /**
     * 医生提交个人信息修改申请
     * POST /doctor/apply-info-update
     */
    @Operation(summary = "提交个人信息修改申请", 
               description = "医生提交修改个人信息的申请，需要管理员审核通过后才会生效")
    @PostMapping("/apply-info-update")
    @PreAuthorize("hasRole('DOCTOR')")
    public Result applyInfoUpdate(@jakarta.validation.Valid @RequestBody DoctorInfoUpdateRequest request) {
        try {
            // 获取当前用户
            String username = SecurityUtils.getCurrentUsername();
            User user = userMapper.selectByUsername(username);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 获取当前医生
            Doctor doctor = doctorMapper.selectByUserId(user.getId());
            if (doctor == null) {
                return Result.error("当前用户不是医生");
            }
            
            // 构建申请DTO
            CreateApplicationRequestDTO dto = new CreateApplicationRequestDTO();
            dto.setRequestType("INFO_UPDATE");
            dto.setDoctorId(doctor.getId());
            dto.setFieldName(request.getFieldName());
            dto.setNewValue(request.getNewValue());
            dto.setReason(request.getReason());
            
            // 创建申请
            ApplicationRequestDetailDTO application = applicationRequestService.createRequest(
                    dto, user.getId(), user.getRole());
            
            return Result.success(application);
        } catch (Exception e) {
            return Result.error("提交失败: " + e.getMessage());
        }
    }
    
    /**
     * 医生查看自己的信息修改申请
     * GET /doctor/my-info-applications
     */
    @Operation(summary = "查看我的信息修改申请", description = "医生查看自己提交的所有个人信息修改申请")
    @GetMapping("/my-info-applications")
    @PreAuthorize("hasRole('DOCTOR')")
    public Result getMyInfoApplications() {
        try {
            // 获取当前用户
            String username = SecurityUtils.getCurrentUsername();
            User user = userMapper.selectByUsername(username);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 查询该用户提交的所有申请
            List<ApplicationRequestDetailDTO> applications = 
                    applicationRequestService.getRequestsByApplicant(user.getId());
            
            // 只返回信息修改类的申请
            List<ApplicationRequestDetailDTO> infoApplications = applications.stream()
                    .filter(app -> "INFO_UPDATE".equals(app.getRequestType()))
                    .toList();
            
            return Result.success(infoApplications);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 医生查看个人信息
     * GET /doctor/my-info
     */
    @Operation(summary = "查看我的个人信息", description = "医生查看自己的详细信息")
    @GetMapping("/my-info")
    @PreAuthorize("hasRole('DOCTOR')")
    public Result getMyInfo() {
        try {
            // 获取当前医生ID
            Long userId = SecurityUtils.getCurrentUserId();
            Doctor doctor = doctorMapper.selectByUserId(userId);
            if (doctor == null) {
                return Result.error("当前用户不是医生");
            }
            
            return Result.success(doctor);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 内部类：医生信息修改申请请求体
     */
    public static class DoctorInfoUpdateRequest {
        @Parameter(description = "要修改的字段名（name/title/specialty/bio）", required = true)
        private String fieldName;
        
        @Parameter(description = "新值", required = true)
        private String newValue;
        
        @Parameter(description = "修改理由", required = true)
        private String reason;
        
        public String getFieldName() {
            return fieldName;
        }
        
        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }
        
        public String getNewValue() {
            return newValue;
        }
        
        public void setNewValue(String newValue) {
            this.newValue = newValue;
        }
        
        public String getReason() {
            return reason;
        }
        
        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}