package com.example.springboot.aspect;

import com.example.springboot.annotation.AuditLog;
import com.example.springboot.common.Result;
import com.example.springboot.config.SecurityUtils;
import com.example.springboot.entity.User;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.service.AuditLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 审计日志切面
 */
@Aspect
@Component
public class AuditLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogAspect.class);
    private final AuditLogService auditLogService;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuditLogAspect(AuditLogService auditLogService, UserMapper userMapper) {
        this.auditLogService = auditLogService;
        this.userMapper = userMapper;
    }

    @Around("@annotation(com.example.springboot.annotation.AuditLog)")
    public Object recordAuditLog(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AuditLog auditLogAnnotation = method.getAnnotation(AuditLog.class);

        long start = System.currentTimeMillis();
        Object result = null;
        boolean success = true;
        String errorMessage = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            success = false;
            errorMessage = ex.getMessage();
            throw ex;
        } finally {
            long executionTime = System.currentTimeMillis() - start;
            try {
                com.example.springboot.entity.AuditLog auditLogEntity =
                        buildAuditLog(joinPoint, auditLogAnnotation, result, success, errorMessage, executionTime);
                auditLogService.save(auditLogEntity);
            } catch (Exception logEx) {
                logger.error("记录审计日志失败", logEx);
            }
        }
    }

    private com.example.springboot.entity.AuditLog buildAuditLog(ProceedingJoinPoint joinPoint,
                                   AuditLog auditAnnotation,
                                   Object result,
                                   boolean success,
                                   String errorMessage,
                                   long executionTime) {
        com.example.springboot.entity.AuditLog log = new com.example.springboot.entity.AuditLog();
        log.setOperationType(auditAnnotation.operationType());
        log.setOperationModule(auditAnnotation.operationModule());
        log.setOperationDesc(auditAnnotation.operationDesc());
        log.setExecutionTime(executionTime);
        log.setStatus(success ? "SUCCESS" : "FAILURE");
        log.setErrorMessage(errorMessage);
        log.setCreatedAt(new Date());

        Long userId = SecurityUtils.getCurrentUserId();
        String username = SecurityUtils.getCurrentUsername();
        String userRole = SecurityUtils.getCurrentUserRole();
        
        // 如果无法从SecurityContext获取角色，尝试从数据库查询
        if (userRole == null && username != null) {
            try {
                User user = userMapper.selectByUsername(username);
                if (user != null) {
                    userRole = user.getRole();
                    if (userId == null) {
                        userId = user.getId();
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to fetch user role from database for username: {}", username, e);
            }
        }
        
        log.setUserId(userId);
        log.setUsername(username);
        log.setUserRole(userRole);

        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            log.setRequestMethod(request.getMethod());
            log.setRequestUrl(request.getRequestURI());
            log.setIpAddress(getClientIp(request));
            log.setUserAgent(request.getHeader("User-Agent"));
            if (auditAnnotation.recordRequest()) {
                log.setRequestParams(buildRequestParams(request, joinPoint));
            }
        } else if (auditAnnotation.recordRequest()) {
            log.setRequestParams(buildArgsPayload(joinPoint.getArgs()));
        }

        if (result instanceof Result res) {
            log.setResponseCode(res.getCode());
            log.setResponseMsg(res.getMsg());
            if (auditAnnotation.recordResponse()) {
                log.setResponseMsg(convertToJsonSafely(result));
            }
        } else if (auditAnnotation.recordResponse()) {
            log.setResponseMsg(convertToJsonSafely(result));
        }

        return log;
    }

    private HttpServletRequest getCurrentRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    private String buildRequestParams(HttpServletRequest request, ProceedingJoinPoint joinPoint) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (!parameterMap.isEmpty()) {
            return convertToJsonSafely(parameterMap);
        }
        return buildArgsPayload(joinPoint.getArgs());
    }

    private String buildArgsPayload(Object[] args) {
        List<Object> filtered = new ArrayList<>();
        if (args != null) {
            for (Object arg : args) {
                if (shouldSkip(arg)) {
                    continue;
                }
                filtered.add(arg);
            }
        }
        return convertToJsonSafely(filtered);
    }

    private boolean shouldSkip(Object arg) {
        return arg instanceof HttpServletRequest
                || arg instanceof HttpServletResponse
                || arg instanceof BindingResult
                || arg instanceof MultipartFile;
    }

    private String convertToJsonSafely(Object data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            logger.debug("序列化审计数据失败: {}", e.getMessage());
            return Objects.toString(data);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

