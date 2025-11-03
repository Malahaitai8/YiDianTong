package com.example.springboot.service;

import com.example.springboot.dto.*;
import com.example.springboot.entity.*;
import com.example.springboot.mapper.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 申请记录服务
 */
@Service
public class ApplicationRequestService {

    @Resource
    private ApplicationRequestMapper applicationRequestMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private DoctorMapper doctorMapper;

    @Resource
    private ScheduleMapper scheduleMapper;

    /**
     * 创建申请
     */
    @Transactional
    public ApplicationRequestDetailDTO createRequest(CreateApplicationRequestDTO dto, Long applicantId, String applicantRole) {
        // 1. 验证申请类型和必填字段
        if ("SCHEDULE_CHANGE".equals(dto.getRequestType())) {
            if (dto.getScheduleId() == null) {
                throw new RuntimeException("调班申请必须指定排班ID");
            }
            if (dto.getChangeType() == null) {
                throw new RuntimeException("调班申请必须指定调班类型");
            }
            
            // 验证排班是否存在
            Schedule schedule = scheduleMapper.selectById(dto.getScheduleId());
            if (schedule == null) {
                throw new RuntimeException("排班不存在");
            }
        } else if ("INFO_UPDATE".equals(dto.getRequestType())) {
            if (dto.getDoctorId() == null) {
                throw new RuntimeException("信息修改申请必须指定医生ID");
            }
            if (dto.getFieldName() == null) {
                throw new RuntimeException("信息修改申请必须指定字段名");
            }
            if (dto.getNewValue() == null) {
                throw new RuntimeException("信息修改申请必须指定新值");
            }
            
            // 验证医生是否存在
            Doctor doctor = doctorMapper.selectById(dto.getDoctorId());
            if (doctor == null) {
                throw new RuntimeException("医生不存在");
            }
            
            // 获取旧值
            String oldValue = getDoctorFieldValue(doctor, dto.getFieldName());
            if (oldValue == null) {
                throw new RuntimeException("字段名无效");
            }
        }

        // 2. 构建实体对象
        ApplicationRequest request = new ApplicationRequest();
        request.setRequestType(dto.getRequestType());
        request.setApplicantId(applicantId);
        request.setApplicantRole(applicantRole);
        request.setScheduleId(dto.getScheduleId());
        request.setChangeType(dto.getChangeType());
        request.setNewDate(dto.getNewDate());
        request.setNewTimeSlot(dto.getNewTimeSlot());
        request.setSlotAdjustment(dto.getSlotAdjustment());
        request.setDoctorId(dto.getDoctorId());
        request.setFieldName(dto.getFieldName());
        request.setNewValue(dto.getNewValue());
        request.setReason(dto.getReason());
        request.setStatus("PENDING");

        // 设置原始信息
        if ("SCHEDULE_CHANGE".equals(dto.getRequestType())) {
            Schedule schedule = scheduleMapper.selectById(dto.getScheduleId());
            request.setOriginalDate(schedule.getScheduleDate());
            request.setOriginalTimeSlot(schedule.getTimeSlot());
        } else if ("INFO_UPDATE".equals(dto.getRequestType())) {
            Doctor doctor = doctorMapper.selectById(dto.getDoctorId());
            request.setOldValue(getDoctorFieldValue(doctor, dto.getFieldName()));
        }

        // 3. 保存到数据库
        applicationRequestMapper.insert(request);

        // 4. 返回详情
        return convertToDetailDTO(applicationRequestMapper.selectById(request.getId()));
    }

    /**
     * 查询所有申请
     */
    public List<ApplicationRequestDetailDTO> getAllRequests() {
        List<ApplicationRequest> requests = applicationRequestMapper.selectAll();
        return requests.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID查询申请
     */
    public ApplicationRequestDetailDTO getRequestById(Long id) {
        ApplicationRequest request = applicationRequestMapper.selectById(id);
        if (request == null) {
            throw new RuntimeException("申请不存在");
        }
        return convertToDetailDTO(request);
    }

    /**
     * 根据ID查询申请详情（直接从数据库联查）
     */
    public ApplicationRequestDetailDTO getRequestDetailById(Long id) {
        ApplicationRequestDetailDTO detail = applicationRequestMapper.selectDetailById(id);
        if (detail == null) {
            throw new RuntimeException("申请不存在");
        }
        return detail;
    }

    /**
     * 根据申请人查询
     */
    public List<ApplicationRequestDetailDTO> getRequestsByApplicant(Long applicantId) {
        List<ApplicationRequest> requests = applicationRequestMapper.selectByApplicantId(applicantId);
        return requests.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据状态查询
     */
    public List<ApplicationRequestDetailDTO> getRequestsByStatus(String status) {
        List<ApplicationRequest> requests = applicationRequestMapper.selectByStatus(status);
        return requests.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * 查询待审核的申请
     */
    public List<ApplicationRequestDetailDTO> getPendingRequests() {
        List<ApplicationRequest> requests = applicationRequestMapper.selectPendingRequests();
        return requests.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * 审核申请
     */
    @Transactional
    public ApplicationRequestDetailDTO reviewRequest(ReviewApplicationRequestDTO dto, Long reviewerId) {
        // 1. 查询申请
        ApplicationRequest request = applicationRequestMapper.selectById(dto.getRequestId());
        if (request == null) {
            throw new RuntimeException("申请不存在");
        }

        // 2. 检查申请状态
        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("申请已经被处理");
        }

        // 3. 检查拒绝原因
        if ("REJECTED".equals(dto.getAction()) && 
            (dto.getRejectReason() == null || dto.getRejectReason().trim().isEmpty())) {
            throw new RuntimeException("拒绝申请必须填写拒绝原因");
        }

        // 4. 如果是批准，执行相应的操作
        if ("APPROVED".equals(dto.getAction())) {
            if ("SCHEDULE_CHANGE".equals(request.getRequestType())) {
                handleScheduleChangeApproval(request);
            } else if ("INFO_UPDATE".equals(request.getRequestType())) {
                handleInfoUpdateApproval(request);
            }
        }

        // 5. 更新申请状态
        applicationRequestMapper.updateStatus(
                dto.getRequestId(),
                dto.getAction(),
                reviewerId,
                new Date(),
                dto.getRejectReason()
        );

        // 6. 返回详情
        return convertToDetailDTO(applicationRequestMapper.selectById(dto.getRequestId()));
    }

    /**
     * 取消申请
     */
    @Transactional
    public void cancelRequest(Long requestId, Long applicantId) {
        ApplicationRequest request = applicationRequestMapper.selectById(requestId);
        if (request == null) {
            throw new RuntimeException("申请不存在");
        }

        if (!request.getApplicantId().equals(applicantId)) {
            throw new RuntimeException("只能取消自己的申请");
        }

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("只能取消待审核的申请");
        }

        applicationRequestMapper.updateStatus(requestId, "CANCELLED", null, new Date(), "申请人取消");
    }

    /**
     * 删除申请
     */
    @Transactional
    public void deleteRequest(Long id) {
        ApplicationRequest request = applicationRequestMapper.selectById(id);
        if (request == null) {
            throw new RuntimeException("申请不存在");
        }
        applicationRequestMapper.deleteById(id);
    }

    /**
     * 统计申请数量
     */
    public Map<String, Integer> getRequestStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("pending", applicationRequestMapper.countByStatus("PENDING"));
        stats.put("approved", applicationRequestMapper.countByStatus("APPROVED"));
        stats.put("rejected", applicationRequestMapper.countByStatus("REJECTED"));
        stats.put("cancelled", applicationRequestMapper.countByStatus("CANCELLED"));
        stats.put("total", applicationRequestMapper.selectAll().size());
        return stats;
    }

    // ========== 私有辅助方法 ==========

    /**
     * 处理调班申请批准
     */
    private void handleScheduleChangeApproval(ApplicationRequest request) {
        Schedule schedule = scheduleMapper.selectById(request.getScheduleId());
        if (schedule == null) {
            throw new RuntimeException("排班不存在");
        }

        switch (request.getChangeType()) {
            case "CANCEL":
                // 取消排班
                scheduleMapper.deleteById(request.getScheduleId());
                break;
                
            case "RESCHEDULE":
                // 改期
                if (request.getNewDate() == null || request.getNewTimeSlot() == null) {
                    throw new RuntimeException("改期申请必须指定新日期和新时间段");
                }
                schedule.setScheduleDate(request.getNewDate());
                schedule.setTimeSlot(request.getNewTimeSlot());
                scheduleMapper.updateById(schedule);
                break;
                
            case "ADJUST_SLOTS":
                // 调整号源
                if (request.getSlotAdjustment() == null) {
                    throw new RuntimeException("调整号源申请必须指定调整数量");
                }
                int newTotalSlots = schedule.getTotalSlots() + request.getSlotAdjustment();
                int newAvailableSlots = schedule.getAvailableSlots() + request.getSlotAdjustment();
                
                if (newTotalSlots < 0 || newAvailableSlots < 0) {
                    throw new RuntimeException("号源数量不能为负数");
                }
                
                schedule.setTotalSlots(newTotalSlots);
                schedule.setAvailableSlots(newAvailableSlots);
                scheduleMapper.updateById(schedule);
                break;
                
            default:
                throw new RuntimeException("未知的调班类型: " + request.getChangeType());
        }
    }

    /**
     * 处理信息修改申请批准
     */
    private void handleInfoUpdateApproval(ApplicationRequest request) {
        Doctor doctor = doctorMapper.selectById(request.getDoctorId());
        if (doctor == null) {
            throw new RuntimeException("医生不存在");
        }

        // 更新医生信息
        setDoctorFieldValue(doctor, request.getFieldName(), request.getNewValue());
        doctorMapper.update(doctor);
    }

    /**
     * 获取医生字段值
     */
    private String getDoctorFieldValue(Doctor doctor, String fieldName) {
        switch (fieldName) {
            case "name": return doctor.getName();
            case "title": return doctor.getTitle();
            case "specialty": return doctor.getSpecialty();
            case "bio": return doctor.getBio();
            default: return null;
        }
    }

    /**
     * 设置医生字段值
     */
    private void setDoctorFieldValue(Doctor doctor, String fieldName, String value) {
        switch (fieldName) {
            case "name": doctor.setName(value); break;
            case "title": doctor.setTitle(value); break;
            case "specialty": doctor.setSpecialty(value); break;
            case "bio": doctor.setBio(value); break;
            default: throw new RuntimeException("未知的字段名: " + fieldName);
        }
    }

    /**
     * 转换为详情 DTO
     */
    private ApplicationRequestDetailDTO convertToDetailDTO(ApplicationRequest request) {
        ApplicationRequestDetailDTO dto = new ApplicationRequestDetailDTO();
        dto.setId(request.getId());
        dto.setRequestType(request.getRequestType());
        dto.setRequestTypeName(getRequestTypeName(request.getRequestType()));
        
        dto.setApplicantId(request.getApplicantId());
        if (request.getApplicant() != null) {
            dto.setApplicantUsername(request.getApplicant().getUsername());
        }
        dto.setApplicantRole(request.getApplicantRole());
        
        dto.setScheduleId(request.getScheduleId());
        dto.setChangeType(request.getChangeType());
        dto.setChangeTypeName(getChangeTypeName(request.getChangeType()));
        dto.setOriginalDate(request.getOriginalDate());
        dto.setOriginalTimeSlot(request.getOriginalTimeSlot());
        dto.setNewDate(request.getNewDate());
        dto.setNewTimeSlot(request.getNewTimeSlot());
        dto.setSlotAdjustment(request.getSlotAdjustment());
        
        dto.setDoctorId(request.getDoctorId());
        if (request.getDoctor() != null) {
            dto.setDoctorName(request.getDoctor().getName());
        }
        dto.setFieldName(request.getFieldName());
        dto.setFieldNameDisplay(getFieldNameDisplay(request.getFieldName()));
        dto.setOldValue(request.getOldValue());
        dto.setNewValue(request.getNewValue());
        
        dto.setStatus(request.getStatus());
        dto.setStatusName(getStatusName(request.getStatus()));
        dto.setReason(request.getReason());
        dto.setRejectReason(request.getRejectReason());
        
        dto.setReviewerId(request.getReviewerId());
        if (request.getReviewer() != null) {
            dto.setReviewerUsername(request.getReviewer().getUsername());
        }
        dto.setReviewedAt(request.getReviewedAt());
        
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());
        
        return dto;
    }

    /**
     * 获取申请类型名称
     */
    private String getRequestTypeName(String requestType) {
        if (requestType == null) return "";
        switch (requestType) {
            case "SCHEDULE_CHANGE": return "调班申请";
            case "INFO_UPDATE": return "信息修改申请";
            default: return requestType;
        }
    }

    /**
     * 获取调班类型名称
     */
    private String getChangeTypeName(String changeType) {
        if (changeType == null) return "";
        switch (changeType) {
            case "CANCEL": return "取消排班";
            case "RESCHEDULE": return "改期";
            case "ADJUST_SLOTS": return "调整号源";
            default: return changeType;
        }
    }

    /**
     * 获取字段名显示
     */
    private String getFieldNameDisplay(String fieldName) {
        if (fieldName == null) return "";
        switch (fieldName) {
            case "name": return "姓名";
            case "title": return "职称";
            case "specialty": return "擅长领域";
            case "bio": return "个人简介";
            default: return fieldName;
        }
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(String status) {
        if (status == null) return "";
        switch (status) {
            case "PENDING": return "待审核";
            case "APPROVED": return "已批准";
            case "REJECTED": return "已拒绝";
            case "CANCELLED": return "已取消";
            default: return status;
        }
    }
}

