package com.example.springboot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要记录审计日志的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /**
     * 操作类型（CREATE/UPDATE/DELETE/APPROVE/REJECT/QUERY等）
     */
    String operationType();

    /**
     * 操作模块（如：SCHEDULE/APPOINTMENT/CONFIG等）
     */
    String operationModule();

    /**
     * 操作描述
     */
    String operationDesc() default "";

    /**
     * 是否记录请求参数
     */
    boolean recordRequest() default true;

    /**
     * 是否记录响应数据
     */
    boolean recordResponse() default false;
}







