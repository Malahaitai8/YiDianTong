package com.example.springboot.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 号源设置 DTO（全局 / 医生 / 门诊通用）
 */
public class ScheduleSettingsDTO {

    private Long doctorId;
    private Long clinicId;
    private List<String> allowedSlotTypes = new ArrayList<>();
    private Integer defaultTotalSlots;
    private Integer maxSlotsPerSchedule;
    private Integer maxAppointmentsPerDayPerDoctor;
    private Integer maxAppointmentsPerDayPerPatient;
    private Integer vipDailyLimitPerDoctor;
    private Boolean enforceWeekendLimits;
    private CancelPolicy cancelPolicy;
    private String overrideStrategy;
    private String effectiveStartDate;
    private String effectiveEndDate;
    private String description;

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public Long getClinicId() {
        return clinicId;
    }

    public void setClinicId(Long clinicId) {
        this.clinicId = clinicId;
    }

    public List<String> getAllowedSlotTypes() {
        return allowedSlotTypes;
    }

    public void setAllowedSlotTypes(List<String> allowedSlotTypes) {
        this.allowedSlotTypes = allowedSlotTypes;
    }

    public Integer getDefaultTotalSlots() {
        return defaultTotalSlots;
    }

    public void setDefaultTotalSlots(Integer defaultTotalSlots) {
        this.defaultTotalSlots = defaultTotalSlots;
    }

    public Integer getMaxSlotsPerSchedule() {
        return maxSlotsPerSchedule;
    }

    public void setMaxSlotsPerSchedule(Integer maxSlotsPerSchedule) {
        this.maxSlotsPerSchedule = maxSlotsPerSchedule;
    }

    public Integer getMaxAppointmentsPerDayPerDoctor() {
        return maxAppointmentsPerDayPerDoctor;
    }

    public void setMaxAppointmentsPerDayPerDoctor(Integer maxAppointmentsPerDayPerDoctor) {
        this.maxAppointmentsPerDayPerDoctor = maxAppointmentsPerDayPerDoctor;
    }

    public Integer getMaxAppointmentsPerDayPerPatient() {
        return maxAppointmentsPerDayPerPatient;
    }

    public void setMaxAppointmentsPerDayPerPatient(Integer maxAppointmentsPerDayPerPatient) {
        this.maxAppointmentsPerDayPerPatient = maxAppointmentsPerDayPerPatient;
    }

    public Integer getVipDailyLimitPerDoctor() {
        return vipDailyLimitPerDoctor;
    }

    public void setVipDailyLimitPerDoctor(Integer vipDailyLimitPerDoctor) {
        this.vipDailyLimitPerDoctor = vipDailyLimitPerDoctor;
    }

    public Boolean getEnforceWeekendLimits() {
        return enforceWeekendLimits;
    }

    public void setEnforceWeekendLimits(Boolean enforceWeekendLimits) {
        this.enforceWeekendLimits = enforceWeekendLimits;
    }

    public CancelPolicy getCancelPolicy() {
        return cancelPolicy;
    }

    public void setCancelPolicy(CancelPolicy cancelPolicy) {
        this.cancelPolicy = cancelPolicy;
    }

    public String getOverrideStrategy() {
        return overrideStrategy;
    }

    public void setOverrideStrategy(String overrideStrategy) {
        this.overrideStrategy = overrideStrategy;
    }

    public String getEffectiveStartDate() {
        return effectiveStartDate;
    }

    public void setEffectiveStartDate(String effectiveStartDate) {
        this.effectiveStartDate = effectiveStartDate;
    }

    public String getEffectiveEndDate() {
        return effectiveEndDate;
    }

    public void setEffectiveEndDate(String effectiveEndDate) {
        this.effectiveEndDate = effectiveEndDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static class CancelPolicy {
        private Integer latestCancelHours;
        private Boolean penaltyEnabled;

        public Integer getLatestCancelHours() {
            return latestCancelHours;
        }

        public void setLatestCancelHours(Integer latestCancelHours) {
            this.latestCancelHours = latestCancelHours;
        }

        public Boolean getPenaltyEnabled() {
            return penaltyEnabled;
        }

        public void setPenaltyEnabled(Boolean penaltyEnabled) {
            this.penaltyEnabled = penaltyEnabled;
        }
    }
}

























