package com.example.springboot.constants;

/**
 * 角色常量类
 * 统一管理系统中的角色名称
 */
public class RoleConstants {
    
    // ========== 数据库存储的角色名 (小写) ==========
    public static final String DB_ROLE_ADMIN = "admin";
    public static final String DB_ROLE_DOCTOR = "doctor";
    public static final String DB_ROLE_PATIENT = "patient";
    
    // ========== Spring Security 角色名 (大写+前缀) ==========
    public static final String SPRING_ROLE_ADMIN = "ROLE_ADMIN";
    public static final String SPRING_ROLE_DOCTOR = "ROLE_DOCTOR";
    public static final String SPRING_ROLE_PATIENT = "ROLE_PATIENT";
    
    // ========== @PreAuthorize 注解中使用的角色名 (大写，无前缀) ==========
    public static final String AUTH_ROLE_ADMIN = "ADMIN";
    public static final String AUTH_ROLE_DOCTOR = "DOCTOR";
    public static final String AUTH_ROLE_PATIENT = "PATIENT";
    
    /**
     * 将数据库角色转换为Spring Security角色
     */
    public static String toSpringRole(String dbRole) {
        if (dbRole == null) return null;
        return "ROLE_" + dbRole.toUpperCase();
    }
    
    /**
     * 将数据库角色转换为认证角色（用于@PreAuthorize）
     */
    public static String toAuthRole(String dbRole) {
        if (dbRole == null) return null;
        return dbRole.toUpperCase();
    }
    
    /**
     * 验证角色是否有效
     */
    public static boolean isValidDbRole(String role) {
        return DB_ROLE_ADMIN.equals(role) || 
               DB_ROLE_DOCTOR.equals(role) || 
               DB_ROLE_PATIENT.equals(role);
    }
}
