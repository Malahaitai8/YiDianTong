package com.example.springboot.service;

import com.example.springboot.dto.ScheduleSettingsDTO;
import com.example.springboot.entity.SystemConfig;
import com.example.springboot.mapper.SystemConfigMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

@Service
public class ScheduleSettingsService {

    private static final String GLOBAL_KEY = "SCHEDULE_SETTINGS_GLOBAL";
    private static final String DOCTOR_KEY_PREFIX = "SCHEDULE_SETTINGS_DOCTOR_";
    private static final String CLINIC_KEY_PREFIX = "SCHEDULE_SETTINGS_CLINIC_";

    @Resource
    private SystemConfigMapper systemConfigMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ScheduleSettingsDTO getGlobalSettings() {
        return loadSettings(GLOBAL_KEY, defaultGlobalSettings());
    }

    public void saveGlobalSettings(ScheduleSettingsDTO dto) {
        saveSettings(GLOBAL_KEY, sanitize(dto));
    }

    public ScheduleSettingsDTO getDoctorSettings(Long doctorId) {
        if (doctorId == null) {
            return null;
        }
        ScheduleSettingsDTO dto = loadSettings(DOCTOR_KEY_PREFIX + doctorId, null);
        if (dto == null) {
            dto = new ScheduleSettingsDTO();
            dto.setDoctorId(doctorId);
        } else {
            dto.setDoctorId(doctorId);
        }
        return dto;
    }

    public void saveDoctorSettings(Long doctorId, ScheduleSettingsDTO dto) {
        if (doctorId == null || dto == null) {
            return;
        }
        dto.setDoctorId(doctorId);
        saveSettings(DOCTOR_KEY_PREFIX + doctorId, sanitize(dto));
    }

    public ScheduleSettingsDTO getClinicSettings(Long clinicId) {
        if (clinicId == null) {
            return null;
        }
        ScheduleSettingsDTO dto = loadSettings(CLINIC_KEY_PREFIX + clinicId, null);
        if (dto == null) {
            dto = new ScheduleSettingsDTO();
            dto.setClinicId(clinicId);
        } else {
            dto.setClinicId(clinicId);
        }
        return dto;
    }

    public void saveClinicSettings(Long clinicId, ScheduleSettingsDTO dto) {
        if (clinicId == null || dto == null) {
            return;
        }
        dto.setClinicId(clinicId);
        saveSettings(CLINIC_KEY_PREFIX + clinicId, sanitize(dto));
    }

    private ScheduleSettingsDTO loadSettings(String key, ScheduleSettingsDTO defaultValue) {
        SystemConfig config = systemConfigMapper.selectByKey(key);
        if (config == null || config.getValue() == null) {
            return defaultValue;
        }
        try {
            ScheduleSettingsDTO dto = objectMapper.readValue(config.getValue(), new TypeReference<ScheduleSettingsDTO>() {});
            return mergeDefaults(dto, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private void saveSettings(String key, ScheduleSettingsDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            int updated = systemConfigMapper.updateValueByKey(key, json);
            if (updated == 0) {
                SystemConfig config = new SystemConfig();
                config.setKey(key);
                config.setValue(json);
                config.setDescription("Schedule settings");
                systemConfigMapper.insert(config);
            }
        } catch (Exception ignored) {
        }
    }

    private ScheduleSettingsDTO sanitize(ScheduleSettingsDTO dto) {
        if (dto == null) {
            return null;
        }
        if (dto.getAllowedSlotTypes() == null) {
            dto.setAllowedSlotTypes(List.of());
        }
        return dto;
    }

    private ScheduleSettingsDTO defaultGlobalSettings() {
        ScheduleSettingsDTO dto = new ScheduleSettingsDTO();
        dto.setAllowedSlotTypes(Arrays.asList("normal", "expert"));
        dto.setDefaultTotalSlots(20);
        dto.setMaxSlotsPerSchedule(50);
        dto.setMaxAppointmentsPerDayPerDoctor(60);
        dto.setMaxAppointmentsPerDayPerPatient(3);
        dto.setVipDailyLimitPerDoctor(10);
        dto.setEnforceWeekendLimits(Boolean.TRUE);
        ScheduleSettingsDTO.CancelPolicy policy = new ScheduleSettingsDTO.CancelPolicy();
        policy.setLatestCancelHours(2);
        policy.setPenaltyEnabled(Boolean.FALSE);
        dto.setCancelPolicy(policy);
        dto.setOverrideStrategy("OVERRIDE");
        return dto;
    }

    private ScheduleSettingsDTO mergeDefaults(ScheduleSettingsDTO source, ScheduleSettingsDTO defaults) {
        if (source == null) {
            return defaults;
        }
        if (CollectionUtils.isEmpty(source.getAllowedSlotTypes()) && defaults != null) {
            source.setAllowedSlotTypes(defaults.getAllowedSlotTypes());
        }
        if (source.getCancelPolicy() == null && defaults != null) {
            source.setCancelPolicy(defaults.getCancelPolicy());
        }
        return source;
    }
}

