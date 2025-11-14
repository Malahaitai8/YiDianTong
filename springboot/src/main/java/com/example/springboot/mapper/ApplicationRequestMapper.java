package com.example.springboot.mapper;

import com.example.springboot.dto.ApplicationRequestDetailDTO;
import com.example.springboot.entity.ApplicationRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 申请记录 Mapper 接口
 */
@Mapper
public interface ApplicationRequestMapper {
    
    /**
     * 查询所有申请记录
     */
    List<ApplicationRequest> selectAll();
    
    /**
     * 根据ID查询申请记录
     */
    ApplicationRequest selectById(Long id);
    
    /**
     * 根据申请类型查询
     */
    List<ApplicationRequest> selectByRequestType(String requestType);
    
    /**
     * 根据申请人ID查询
     */
    List<ApplicationRequest> selectByApplicantId(Long applicantId);
    
    /**
     * 根据状态查询
     */
    List<ApplicationRequest> selectByStatus(String status);
    
    /**
     * 根据医生ID查询（信息修改申请）
     */
    List<ApplicationRequest> selectByDoctorId(Long doctorId);
    
    /**
     * 根据排班ID查询（调班申请）
     */
    List<ApplicationRequest> selectByScheduleId(Long scheduleId);
    
    /**
     * 查询待审核的申请
     */
    List<ApplicationRequest> selectPendingRequests();
    
    /**
     * 查询指定日期范围内的申请
     */
    List<ApplicationRequest> selectByDateRange(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );
    
    /**
     * 组合条件查询
     */
    List<ApplicationRequest> selectByConditions(
            @Param("requestType") String requestType,
            @Param("status") String status,
            @Param("applicantId") Long applicantId,
            @Param("doctorId") Long doctorId
    );
    
    /**
     * 插入申请记录
     */
    int insert(ApplicationRequest applicationRequest);
    
    /**
     * 更新申请记录
     */
    int update(ApplicationRequest applicationRequest);
    
    /**
     * 更新申请状态
     */
    int updateStatus(
            @Param("id") Long id,
            @Param("status") String status,
            @Param("reviewerId") Long reviewerId,
            @Param("reviewedAt") Date reviewedAt,
            @Param("rejectReason") String rejectReason
    );
    
    /**
     * 删除申请记录
     */
    int deleteById(Long id);
    
    /**
     * 批量删除申请记录
     */
    int deleteBatch(@Param("ids") List<Long> ids);
    
    /**
     * 统计申请数量
     */
    int countByStatus(String status);
    
    /**
     * 统计指定申请人的申请数量
     */
    int countByApplicant(@Param("applicantId") Long applicantId, @Param("status") String status);

    /**
     * 查询申请详情（包含关联信息）
     */
    ApplicationRequestDetailDTO selectDetailById(Long id);
}

