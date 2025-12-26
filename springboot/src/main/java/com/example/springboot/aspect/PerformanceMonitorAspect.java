package com.example.springboot.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 性能监控切面
 * 监控关键业务方法的执行时间
 */
@Aspect
@Component
public class PerformanceMonitorAspect {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitorAspect.class);

    /**
     * 监控AppointmentController的所有方法
     */
    @Pointcut("execution(* com.example.springboot.controller.AppointmentController.*(..))")
    public void appointmentControllerMethods() {}

    /**
     * 监控AppointmentService的关键业务方法
     */
    @Pointcut("execution(* com.example.springboot.service.AppointmentService.create(..)) || " +
              "execution(* com.example.springboot.service.AppointmentService.searchAvailableSlots(..))")
    public void appointmentServiceMethods() {}

    @Around("appointmentControllerMethods() || appointmentServiceMethods()")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getDeclaringTypeName() + "." +
                           joinPoint.getSignature().getName();

        try {
            Object result = joinPoint.proceed();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // 记录慢请求 (>500ms)
            if (duration > 500) {
                logger.warn("慢请求检测: {} 执行时间: {}ms", methodName, duration);
            } else if (duration > 2000) {
                logger.error("超慢请求: {} 执行时间: {}ms", methodName, duration);
            } else {
                logger.info("方法执行: {} 耗时: {}ms", methodName, duration);
            }

            return result;

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            logger.error("方法执行异常: {} 耗时: {}ms, 错误: {}",
                        methodName, duration, e.getMessage());
            throw e;
        }
    }
}
