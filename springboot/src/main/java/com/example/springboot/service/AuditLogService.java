package com.example.springboot.service;

import com.example.springboot.dto.AuditLogQueryRequest;
import com.example.springboot.entity.AuditLog;
import com.example.springboot.mapper.AuditLogMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuditLogService {

    @Resource
    private AuditLogMapper auditLogMapper;

    /**
     * 保存审计日志
     */
    public void save(AuditLog auditLog) {
        if (auditLog == null) {
            return;
        }
        auditLogMapper.insert(auditLog);
    }

    /**
     * 查询单条审计日志
     */
    public AuditLog getById(Long id) {
        if (id == null) {
            return null;
        }
        return auditLogMapper.selectById(id);
    }

    /**
     * 条件查询审计日志（分页）
     */
    public Map<String, Object> queryLogs(AuditLogQueryRequest request) {
        int page = request.getPage() <= 0 ? 1 : request.getPage();
        int pageSize = request.getPageSize() <= 0 ? 20 : Math.min(request.getPageSize(), 200);
        int offset = (page - 1) * pageSize;

        List<AuditLog> list = auditLogMapper.selectByConditions(
                request.getOperationType(),
                request.getOperationModule(),
                request.getUserId(),
                request.getUsername(),
                request.getUserRole(),
                request.getStatus(),
                request.getTargetType(),
                request.getTargetId(),
                request.getStartTime(),
                request.getEndTime(),
                offset,
                pageSize
        );

        long total = auditLogMapper.countByConditions(
                request.getOperationType(),
                request.getOperationModule(),
                request.getUserId(),
                request.getUsername(),
                request.getUserRole(),
                request.getStatus(),
                request.getTargetType(),
                request.getTargetId(),
                request.getStartTime(),
                request.getEndTime()
        );

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", total == 0 ? 0 : (int) Math.ceil((double) total / pageSize));
        return result;
    }

    /**
     * 获取简单的统计数据（按模块或操作类型）
     */
    public Map<String, Object> buildSummary(AuditLogQueryRequest request) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("total", auditLogMapper.countByConditions(
                request.getOperationType(),
                request.getOperationModule(),
                request.getUserId(),
                request.getUsername(),
                request.getUserRole(),
                request.getStatus(),
                request.getTargetType(),
                request.getTargetId(),
                request.getStartTime(),
                request.getEndTime()
        ));
        summary.put("operationType", request.getOperationType());
        summary.put("operationModule", request.getOperationModule());
        return summary;
    }
}


