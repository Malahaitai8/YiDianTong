package com.example.springboot.config;

import com.example.springboot.constants.RoleConstants;
import com.example.springboot.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Security工具类
 * 用于获取当前登录用户信息
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户
     */
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUser();
        }
        return null;
    }

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前登录用户名
     */
    public static String getCurrentUsername() {
        User user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 获取当前登录用户角色
     */
    public static String getCurrentUserRole() {
        User user = getCurrentUser();
        return user != null ? user.getRole() : null;
    }

    /**
     * 判断当前用户是否为患者
     */
    public static boolean isPatient() {
        return RoleConstants.DB_ROLE_PATIENT.equals(getCurrentUserRole());
    }

    /**
     * 判断当前用户是否为医生
     */
    public static boolean isDoctor() {
        return RoleConstants.DB_ROLE_DOCTOR.equals(getCurrentUserRole());
    }

    /**
     * 判断当前用户是否为管理员
     */
    public static boolean isAdmin() {
        return RoleConstants.DB_ROLE_ADMIN.equals(getCurrentUserRole());
    }
}

