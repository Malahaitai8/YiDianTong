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
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
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

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }


    @Operation(summary = "查询所有医生", description = "获取系统中所有医生信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    @GetMapping("/selectAll")
    @PreAuthorize("isAuthenticated()") // <-- [新增] 任何登录用户
    public Result selectAll() {
        List<Doctor> list = doctorService.selectAll();
        return Result.success(list);
    }

    @Operation(summary = "根据ID查询医生", description = "通过医生ID获取医生详情")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "404", description = "医生不存在")
    })
    @GetMapping("/selectById/{id}")
    @PreAuthorize("isAuthenticated()") // <-- [新增] 任何登录用户
    public Result selectById(
            @Parameter(description = "医生ID", required = true) @PathVariable Long id) {
        Doctor doctor = doctorService.selectById(id);
        return Result.success(doctor);
    }

    @Operation(summary = "新增医生", description = "仅管理员可创建医生记录")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "创建成功",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // <-- [新增] 仅管理员
    public Result create(
            @jakarta.validation.Valid
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "医生对象，包含姓名、职称、专业等字段",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Doctor.class))
            )
            Doctor doctor) {
        doctorService.create(doctor);
        return Result.success();
    }

    @Operation(summary = "更新医生", description = "管理员或医生本人可更新医生信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "更新成功",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "404", description = "医生不存在")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public Result update(
            @Parameter(description = "医生ID", required = true) @PathVariable Long id,
            @jakarta.validation.Valid
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "医生对象，允许更新的字段将被保存",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Doctor.class))
            )
            Doctor doctor) {
        doctor.setId(id); // 使用路径参数中的ID
        doctorService.update(doctor); // Service层已实现本人权限校验
        return Result.success();
    }

    @Operation(summary = "删除医生", description = "仅管理员可删除医生记录")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "删除成功",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "404", description = "医生不存在")
    })
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
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
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
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
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
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
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

    // ==================== 患者/前端：医生详情页坐诊时间 ====================

    /**
     * 前端患者在医生详情页查看该医生的坐诊时间
     * GET /doctor/{id}/schedules
     * - 支持按日期范围与时间段筛选
     * - 返回字段与医生端“我的排班”一致，便于前端复用
     */
    @Operation(summary = "医生坐诊时间（患者端）", 
               description = "患者在医生详情页查看该医生的坐诊安排；支持按日期范围与时间段筛选。\n"
                           + "注意：\n"
                           + "1) 未传 startDate/endDate 时，默认从“今天”起的未来 30 天；\n"
                           + "2) endDate 会被规范化为“次日零点”，以便包含传入的当天；\n"
                           + "3) timeSlot 兼容英文 MORNING/AFTERNOON/EVENING 与中文 上午/下午/晚上；\n"
                           + "4) 需要登录（任意角色），用于前端展示。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "404", description = "医生不存在")
    })
    @GetMapping("/{id}/schedules")
    @PreAuthorize("permitAll()") // 前端医生详情页需要匿名可访问
    public Result getDoctorSchedulesForPatient(
            @Parameter(description = "医生ID", required = true, example = "123") @PathVariable Long id,
            @Parameter(description = "开始日期（格式：yyyy-MM-dd），默认今天", example = "2025-11-01")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期（格式：yyyy-MM-dd），默认 startDate 起未来30天", example = "2025-11-30")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @Parameter(description = "时间段；兼容 MORNING/AFTERNOON/EVENING 与 上午/下午/晚上", 
                       schema = @Schema(allowableValues = {"MORNING","AFTERNOON","EVENING","上午","下午","晚上"}), 
                       example = "MORNING")
            @RequestParam(required = false) String timeSlot) {
        try {
            // 校验医生是否存在
            Doctor doctor = doctorService.selectById(id);
            if (doctor == null) {
                return Result.error("医生不存在");
            }
            // 直接复用医生端“我的排班”服务方法（入参为指定 doctorId）
            Map<String, Object> result = doctorService.getMySchedules(id, startDate, endDate, timeSlot);
            return Result.success(result);
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
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "提交成功",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    @PostMapping("/apply-info-update")
    @PreAuthorize("hasRole('DOCTOR')")
    public Result applyInfoUpdate(
            @jakarta.validation.Valid
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "信息修改申请体",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DoctorInfoUpdateRequest.class))
            )
            DoctorInfoUpdateRequest request) {
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
     * 前端兼容：提交医生信息变更（支持多字段一次提交）
     * 前端调用路径：POST /api/doctor/change-request
     * 注意：本控制器类级路径为 /doctor，因此此处方法路径定义为 /change-request，
     * 若服务全局 context-path 为 /api，则完整路径为 /api/doctor/change-request。
     */
    @Operation(summary = "提交医生信息变更（前端兼容）", 
               description = "接受前端的 name/title/specialization/bio/clinicId/reason，批量创建信息更新申请")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "提交成功",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    @PostMapping("/change-request")
    @PreAuthorize("hasRole('DOCTOR')")
    public Result applyInfoUpdateFrontend(
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "前端兼容信息变更请求体，至少包含一个可变更字段",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FrontendDoctorChangeRequest.class))
            )
            FrontendDoctorChangeRequest request) {
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
            
            // 处理理由，允许为空则给默认
            String reason = trimToNull(request.getReason());
            if (reason == null) {
                reason = "医生端提交信息更新";
            }
            
            // 收集前端可提交字段并做映射
            java.util.LinkedHashMap<String, String> fields = new java.util.LinkedHashMap<>();
            if (request.getName() != null) {
                fields.put("name", request.getName().trim());
            }
            if (request.getTitle() != null) {
                fields.put("title", request.getTitle().trim());
            }
            // specialization -> specialty
            if (request.getSpecialization() != null) {
                fields.put("specialty", request.getSpecialization().trim());
            }
            if (request.getBio() != null) {
                fields.put("bio", request.getBio().trim());
            }
            // clinicId 为前端字段，这里忽略不创建申请
            
            if (fields.isEmpty()) {
                return Result.error("未检测到可提交的变更字段");
            }
            
            java.util.List<ApplicationRequestDetailDTO> created = new java.util.ArrayList<>();
            for (java.util.Map.Entry<String, String> entry : fields.entrySet()) {
                String fieldName = entry.getKey();
                String newValue = entry.getValue();
                
                CreateApplicationRequestDTO dto = new CreateApplicationRequestDTO();
                dto.setRequestType("INFO_UPDATE");
                dto.setDoctorId(doctor.getId());
                dto.setFieldName(fieldName);
                dto.setNewValue(newValue);
                dto.setReason(reason);
                
                ApplicationRequestDetailDTO detail = applicationRequestService.createRequest(
                        dto, user.getId(), user.getRole());
                created.add(detail);
            }
            
            java.util.HashMap<String, Object> resp = new java.util.HashMap<>();
            resp.put("createdCount", created.size());
            resp.put("items", created);
            return Result.success(resp);
        } catch (Exception e) {
            return Result.error("提交失败: " + e.getMessage());
        }
    }
    
    /**
     * 医生查看自己的信息修改申请
     * GET /doctor/my-info-applications
     */
    @Operation(summary = "查看我的信息修改申请", description = "医生查看自己提交的所有个人信息修改申请")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
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
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
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
        @Schema(description = "要修改的字段名（name/title/specialty/bio）", example = "title", requiredMode = Schema.RequiredMode.REQUIRED)
        private String fieldName;
        
        @Schema(description = "新值", example = "主任医师", requiredMode = Schema.RequiredMode.REQUIRED)
        private String newValue;
        
        @Schema(description = "修改理由", example = "职称升级", requiredMode = Schema.RequiredMode.REQUIRED)
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
    
    /**
     * 前端兼容请求体
     */
    public static class FrontendDoctorChangeRequest {
        @Schema(description = "新的姓名", example = "张三")
        private String name;
        
        @Schema(description = "新的职称", example = "副主任医师")
        private String title;
        
        @Schema(description = "新的专业方向（前端字段名 specialization）", example = "心内科")
        private String specialization;
        
        @Schema(description = "新的个人简介", example = "从业10年以上，擅长心血管疾病诊治")
        private String bio;
        
        @Schema(description = "所属门诊ID（忽略）", example = "123")
        private String clinicId;
        
        @Schema(description = "修改理由（可选）", example = "完善信息")
        private String reason;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSpecialization() {
            return specialization;
        }

        public void setSpecialization(String specialization) {
            this.specialization = specialization;
        }

        public String getBio() {
            return bio;
        }

        public void setBio(String bio) {
            this.bio = bio;
        }

        public String getClinicId() {
            return clinicId;
        }

        public void setClinicId(String clinicId) {
            this.clinicId = clinicId;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}