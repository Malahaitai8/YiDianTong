package com.example.springboot.mapper;

import com.example.springboot.dto.ScheduleRuleDetailDTO;
import com.example.springboot.entity.ScheduleRule;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 排班规则 Mapper 接口
 */
public interface ScheduleRuleMapper {
    
    /**
     * 查询所有排班规则
     */
    List<ScheduleRule> selectAll();
    
    /**
     * 根据ID查询排班规则
     */
    ScheduleRule selectById(Long id);
    
    /**
     * 根据医生ID查询排班规则
     */
    List<ScheduleRule> selectByDoctorId(Long doctorId);
    
    /**
     * 根据科室ID查询排班规则
     */
    List<ScheduleRule> selectByDepartmentId(Long departmentId);
    
    /**
     * 根据门诊ID查询排班规则
     */
    List<ScheduleRule> selectByClinicId(Long clinicId);
    
    /**
     * 根据状态查询排班规则
     */
    List<ScheduleRule> selectByStatus(String status);
    
    /**
     * 查询激活状态的规则
     */
    List<ScheduleRule> selectActiveRules();
    
    /**
     * 查询指定医生在指定日期范围内的规则
     */
    List<ScheduleRule> selectByDoctorAndDateRange(
            @Param("doctorId") Long doctorId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );
    
    /**
     * 插入排班规则
     */
    int insert(ScheduleRule scheduleRule);
    
    /**
     * 更新排班规则
     */
    int update(ScheduleRule scheduleRule);
    
    /**
     * 更新规则状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    /**
     * 删除排班规则
     */
    int deleteById(Long id);
    
    /**
     * 批量删除排班规则
     */
    int deleteBatch(@Param("ids") List<Long> ids);
    
    /**
     * 检查规则是否存在冲突
     */
    int checkConflict(
            @Param("doctorId") Long doctorId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("weekDays") String weekDays,
            @Param("timeSlots") String timeSlots,
            @Param("excludeId") Long excludeId
    );

    /**
     * 查询排班规则详情（包含关联信息）
     */
    ScheduleRuleDetailDTO selectDetailById(Long id);
}

