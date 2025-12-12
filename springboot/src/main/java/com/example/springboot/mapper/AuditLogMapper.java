package com.example.springboot.mapper;

import com.example.springboot.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface AuditLogMapper {
    /**
     * 插入审计日志
     */
    int insert(AuditLog auditLog);

    /**
     * 根据ID查询审计日志
     */
    AuditLog selectById(@Param("id") Long id);

    /**
     * 根据用户ID查询审计日志
     */
    List<AuditLog> selectByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 根据操作类型查询审计日志
     */
    List<AuditLog> selectByOperationType(@Param("operationType") String operationType,
                                         @Param("startDate") Date startDate,
                                         @Param("endDate") Date endDate,
                                         @Param("limit") Integer limit);

    /**
     * 根据操作模块查询审计日志
     */
    List<AuditLog> selectByModule(@Param("operationModule") String operationModule,
                                  @Param("startDate") Date startDate,
                                  @Param("endDate") Date endDate,
                                  @Param("limit") Integer limit);

    /**
     * 根据目标对象查询审计日志
     */
    List<AuditLog> selectByTarget(@Param("targetType") String targetType, @Param("targetId") Long targetId);

    /**
     * 根据日期范围查询审计日志
     */
    List<AuditLog> selectByDateRange(@Param("startDate") Date startDate,
                                     @Param("endDate") Date endDate,
                                     @Param("limit") Integer limit);

    /**
     * 统计日期范围内的审计日志数量
     */
    Long countByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 条件查询（分页）
     */
    List<AuditLog> selectByConditions(@Param("operationType") String operationType,
                                      @Param("operationModule") String operationModule,
                                      @Param("userId") Long userId,
                                      @Param("username") String username,
                                      @Param("userRole") String userRole,
                                      @Param("status") String status,
                                      @Param("targetType") String targetType,
                                      @Param("targetId") Long targetId,
                                      @Param("startDate") Date startDate,
                                      @Param("endDate") Date endDate,
                                      @Param("offset") Integer offset,
                                      @Param("limit") Integer limit);

    /**
     * 统计条件查询数量
     */
    Long countByConditions(@Param("operationType") String operationType,
                           @Param("operationModule") String operationModule,
                           @Param("userId") Long userId,
                           @Param("username") String username,
                           @Param("userRole") String userRole,
                           @Param("status") String status,
                           @Param("targetType") String targetType,
                           @Param("targetId") Long targetId,
                           @Param("startDate") Date startDate,
                           @Param("endDate") Date endDate);
}


