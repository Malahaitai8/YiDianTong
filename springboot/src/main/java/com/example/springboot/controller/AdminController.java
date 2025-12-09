package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.constants.RoleConstants;
import com.example.springboot.entity.Admin;
import com.example.springboot.entity.Whitelist;
import com.example.springboot.service.AdminService;
import com.example.springboot.service.WhitelistService;
import com.example.springboot.dto.DoctorCreateRequest;
import com.example.springboot.entity.User;
import com.example.springboot.mapper.UserMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.annotation.Resource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import com.example.springboot.dto.ResetPasswordRequest;

@Tag(name = "管理员管理", description = "管理员相关功能接口")
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearer-jwt")
public class AdminController {

    @Resource
    private AdminService adminService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private com.example.springboot.mapper.DoctorMapper doctorMapper;

    @Resource
    private WhitelistService whitelistService;


    @Operation(summary = "查询所有管理员", description = "管理员列表")
    @GetMapping("/selectAll")
    public Result selectAll() {

        List<Admin> list = adminService.selectAll();

        return Result.success(list);
    }

    @Operation(summary = "根据ID查询管理员", description = "管理员详情")
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Long id) {

        Admin admin = adminService.selectById(id);
        return Result.success(admin);
    }

    /**
     * 管理员创建医生账号
     */
    @Operation(summary = "创建医生账号", description = "管理员为医生创建登录账号及医生详细信息")
    @PostMapping("/doctor/create")
    public Result createDoctor(@jakarta.validation.Valid @RequestBody DoctorCreateRequest request) {
        try {
            // 检查用户名重复
            if (userMapper.selectByUsername(request.getUsername()) != null) {
                return Result.error("用户名已存在");
            }
            
            // 1. 创建用户账号
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(RoleConstants.DB_ROLE_DOCTOR);
            // 管理员创建的医生账号直接激活，无需审核
            user.setStatus("active");
            userMapper.insert(user);
            
            // 2. 创建医生详细信息
            com.example.springboot.entity.Doctor doctor = new com.example.springboot.entity.Doctor();
            doctor.setUserId(user.getId());
            doctor.setClinicId(request.getClinicId());
            doctor.setName(request.getName());
            doctor.setTitle(request.getTitle());
            doctor.setSpecialty(request.getSpecialty());
            doctor.setBio(request.getBio());
            
            doctorMapper.insert(doctor);
            
            return Result.success("医生账号创建成功，账号已激活，可以直接登录");
        } catch (org.springframework.dao.DuplicateKeyException e) {
            return Result.error("用户名已存在，请使用其他用户名");
        } catch (Exception e) {
            return Result.error("创建医生账号失败: " + e.getMessage());
        }
    }

    /**
     * 停用医生账号
     */
    @Operation(summary = "停用医生账号", description = "将医生账号状态设置为disabled")
    @PostMapping("/doctor/{id}/disable")
    public Result disableDoctor(@PathVariable Long id) {
        userMapper.updateStatus(id, "disabled");
        return Result.success("医生账号已停用");
    }

    /**
     * 重置医生密码
     */
    @Operation(summary = "重置医生密码", description = "重置指定医生的登录密码")
    @PostMapping("/doctor/{id}/reset-password")
    public Result resetPassword(@PathVariable Long id, @jakarta.validation.Valid @RequestBody ResetPasswordRequest request) {
        userMapper.updatePassword(id, passwordEncoder.encode(request.getNewPassword()));
        return Result.success("密码已重置");
    }

    /**
     * 管理员将所有医生密码重置为 123456
     */
    @Operation(summary = "重置所有医生密码", description = "管理员一键重置所有医生密码为123456")
    @PostMapping("/doctor/reset-all-passwords")
    public Result resetAllDoctorPasswords() {
        String encoded = passwordEncoder.encode("123456");
        int affected = userMapper.updatePasswordsByRole(RoleConstants.DB_ROLE_DOCTOR, encoded);
        return Result.success("已重置密码的医生账号数量: " + affected);
    }

    // ==================== 白名单管理接口 ====================

    @Operation(summary = "查询所有白名单", description = "管理员查询所有白名单记录")
    @GetMapping("/whitelist/selectAll")
    public Result selectAllWhitelist() {
        try {
            List<Whitelist> list = whitelistService.selectAll();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @Operation(summary = "根据ID查询白名单", description = "管理员查询指定白名单记录")
    @GetMapping("/whitelist/selectById/{id}")
    public Result selectWhitelistById(@PathVariable Long id) {
        try {
            Whitelist whitelist = whitelistService.selectById(id);
            return Result.success(whitelist);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @Operation(summary = "根据角色类型查询白名单", description = "管理员根据角色类型（student/teacher/outsider）查询白名单")
    @GetMapping("/whitelist/selectByRoleType/{roleType}")
    public Result selectWhitelistByRoleType(@PathVariable String roleType) {
        try {
            List<Whitelist> list = whitelistService.selectByRoleType(roleType);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @Operation(summary = "添加白名单", description = "管理员添加学号/工号到白名单")
    @PostMapping("/whitelist")
    public Result createWhitelist(@jakarta.validation.Valid @RequestBody Whitelist whitelist) {
        try {
            whitelistService.create(whitelist);
            return Result.success("白名单添加成功");
        } catch (Exception e) {
            return Result.error("添加失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新白名单", description = "管理员更新白名单记录")
    @org.springframework.web.bind.annotation.PutMapping("/whitelist/{id}")
    public Result updateWhitelist(@PathVariable Long id, @jakarta.validation.Valid @RequestBody Whitelist whitelist) {
        try {
            whitelist.setId(id);
            whitelistService.update(whitelist);
            return Result.success("白名单更新成功");
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除白名单", description = "管理员删除白名单记录")
    @org.springframework.web.bind.annotation.DeleteMapping("/whitelist/{id}")
    public Result deleteWhitelist(@PathVariable Long id) {
        try {
            whitelistService.delete(id);
            return Result.success("白名单删除成功");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量删除白名单", description = "管理员批量删除白名单记录")
    @org.springframework.web.bind.annotation.DeleteMapping("/whitelist/batch")
    public Result batchDeleteWhitelist(@RequestBody List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的记录");
            }
            whitelistService.batchDelete(ids);
            return Result.success("批量删除成功，共删除 " + ids.size() + " 条记录");
        } catch (Exception e) {
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }

    @Operation(summary = "切换白名单状态", description = "管理员切换白名单启用/停用状态")
    @org.springframework.web.bind.annotation.PatchMapping("/whitelist/{id}/status")
    public Result toggleWhitelistStatus(@PathVariable Long id) {
        try {
            whitelistService.toggleStatus(id);
            return Result.success("状态切换成功");
        } catch (Exception e) {
            return Result.error("状态切换失败: " + e.getMessage());
        }
    }
}

