package com.example.springboot.service;

import com.example.springboot.dto.ApproveDoctorChangeRequest;
import com.example.springboot.dto.DoctorChangeRequestDTO;
import com.example.springboot.dto.DoctorChangeRequestItem;
import com.example.springboot.entity.Doctor;
import com.example.springboot.entity.SystemConfig;
import com.example.springboot.mapper.ClinicMapper;
import com.example.springboot.mapper.DoctorMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class DoctorChangeRequestService {

    private static final String CONFIG_KEY = "doctor_change_requests";

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private DoctorMapper doctorMapper;

    @Resource
    private ClinicMapper clinicMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> loadConfig() {
        SystemConfig cfg = systemConfigService.selectByKey(CONFIG_KEY);
        if (cfg == null) {
            Map<String, Object> init = new HashMap<>();
            init.put("version", 1);
            init.put("items", new ArrayList<>());
            writeConfig(init);
            return init;
        }
        String value = cfg.getValue();
        if (!StringUtils.hasText(value)) {
            Map<String, Object> init = new HashMap<>();
            init.put("version", 1);
            init.put("items", new ArrayList<>());
            writeConfig(init);
            return init;
        }
        try {
            return objectMapper.readValue(value, new TypeReference<Map<String, Object>>(){});
        } catch (Exception e) {
            throw new RuntimeException("Invalid doctor_change_requests JSON", e);
        }
    }

    private void writeConfig(Map<String, Object> content) {
        try {
            String json = objectMapper.writeValueAsString(content);
            SystemConfig existing = systemConfigService.selectByKey(CONFIG_KEY);
            if (existing == null) {
                SystemConfig sc = new SystemConfig();
                sc.setKey(CONFIG_KEY);
                sc.setValue(json);
                sc.setDescription("Doctor change requests queue");
                systemConfigService.insert(sc);
            } else {
                systemConfigService.updateValueByKey(CONFIG_KEY, json);
            }
        } catch (Exception e) {
            throw new RuntimeException("Persist doctor_change_requests failed", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getItems(Map<String, Object> cfg) {
        Object items = cfg.get("items");
        if (items == null) {
            List<Map<String, Object>> list = new ArrayList<>();
            cfg.put("items", list);
            return list;
        }
        return (List<Map<String, Object>>) items;
    }

    public DoctorChangeRequestItem submit(DoctorChangeRequestDTO dto) {
        // basic validation
        if (dto.getRequestedByUserId() == null) {
            throw new IllegalArgumentException("requestedByUserId is required");
        }

        // derive doctorId from current user if missing
        if (dto.getDoctorId() == null) {
            Doctor currentDoctor = doctorMapper.selectByUserId(dto.getRequestedByUserId());
            if (currentDoctor == null) {
                throw new IllegalArgumentException("doctor not found for current user");
            }
            dto.setDoctorId(currentDoctor.getId());
        }

        // resolve clinic by name if clinicId not provided
        if (dto.getClinicId() == null && StringUtils.hasText(dto.getClinicName())) {
            com.example.springboot.entity.Clinic clinic = clinicMapper.selectByName(dto.getClinicName());
            if (clinic == null) {
                throw new IllegalArgumentException("clinicName not found");
            }
            dto.setClinicId(clinic.getId());
        }

        if (dto.getClinicId() != null && clinicMapper.selectById(dto.getClinicId()) == null) {
            throw new IllegalArgumentException("clinicId not found");
        }

        Map<String, Object> cfg = loadConfig();
        List<Map<String, Object>> items = getItems(cfg);

        String id = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        Map<String, Object> entry = new HashMap<>();
        entry.put("id", id);
        entry.put("doctorId", dto.getDoctorId());
        entry.put("requestedByUserId", dto.getRequestedByUserId());
        entry.put("clinicId", dto.getClinicId());
        entry.put("name", dto.getName());
        entry.put("title", dto.getTitle());
        entry.put("specialty", dto.getSpecialty());
        entry.put("bio", dto.getBio());
        entry.put("status", "PENDING");
        entry.put("createdAt", now);
        entry.put("updatedAt", now);
        items.add(entry);

        // bump version and persist
        int version = ((Number) cfg.getOrDefault("version", 1)).intValue();
        cfg.put("version", version + 1);
        writeConfig(cfg);

        return toItem(entry);
    }

    public List<DoctorChangeRequestItem> listAll(Optional<String> statusOpt) {
        Map<String, Object> cfg = loadConfig();
        List<Map<String, Object>> items = getItems(cfg);
        List<DoctorChangeRequestItem> result = new ArrayList<>();
        for (Map<String, Object> it : items) {
            DoctorChangeRequestItem item = toItem(it);
            if (statusOpt.isPresent()) {
                if (statusOpt.get().equalsIgnoreCase(item.getStatus())) {
                    result.add(item);
                }
            } else {
                result.add(item);
            }
        }
        return result;
    }

    public List<DoctorChangeRequestItem> listByDoctor(Long doctorId) {
        Map<String, Object> cfg = loadConfig();
        List<Map<String, Object>> items = getItems(cfg);
        List<DoctorChangeRequestItem> result = new ArrayList<>();
        for (Map<String, Object> it : items) {
            if (Objects.equals(asLong(it.get("doctorId")), doctorId)) {
                result.add(toItem(it));
            }
        }
        return result;
    }

    public List<DoctorChangeRequestItem> listByCurrentDoctor(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        Doctor doctor = doctorMapper.selectByUserId(userId);
        if (doctor == null || doctor.getId() == null) {
            return Collections.emptyList();
        }
        return listByDoctor(doctor.getId());
    }

    public DoctorChangeRequestItem review(ApproveDoctorChangeRequest req, String adminUsername) {
        Map<String, Object> cfg = loadConfig();
        List<Map<String, Object>> items = getItems(cfg);
        Map<String, Object> target = null;
        for (Map<String, Object> it : items) {
            if (Objects.equals(String.valueOf(it.get("id")), req.getId())) {
                target = it;
                break;
            }
        }
        if (target == null) {
            throw new IllegalArgumentException("request not found");
        }

        String action = req.getAction();
        if (!"APPROVE".equalsIgnoreCase(action) && !"REJECT".equalsIgnoreCase(action)) {
            throw new IllegalArgumentException("action must be APPROVE or REJECT");
        }

        if ("APPROVE".equalsIgnoreCase(action)) {
            // apply changes
            Long doctorId = asLong(target.get("doctorId"));
            Doctor doc = doctorMapper.selectById(doctorId);
            if (doc == null) {
                throw new IllegalStateException("doctor not found");
            }
            if (target.get("clinicId") != null) {
                Long clinicId = asLong(target.get("clinicId"));
                if (clinicId != null) {
                    if (clinicMapper.selectById(clinicId) == null) {
                        throw new IllegalArgumentException("clinicId not found");
                    }
                    doc.setClinicId(clinicId);
                }
            }
            if (target.get("name") != null) doc.setName((String) target.get("name"));
            if (target.get("title") != null) doc.setTitle((String) target.get("title"));
            if (target.get("specialty") != null) doc.setSpecialty((String) target.get("specialty"));
            if (target.get("bio") != null) doc.setBio((String) target.get("bio"));
            doctorMapper.update(doc);

            target.put("status", "APPROVED");
            target.put("approvedBy", adminUsername);
            target.put("approvedAt", System.currentTimeMillis());
            target.put("reason", null);
        } else {
            target.put("status", "REJECTED");
            target.put("approvedBy", adminUsername);
            target.put("approvedAt", System.currentTimeMillis());
            target.put("reason", req.getReason());
        }
        target.put("updatedAt", System.currentTimeMillis());

        int version = ((Number) cfg.getOrDefault("version", 1)).intValue();
        cfg.put("version", version + 1);
        writeConfig(cfg);
        return toItem(target);
    }

    private DoctorChangeRequestItem toItem(Map<String, Object> it) {
        DoctorChangeRequestItem item = new DoctorChangeRequestItem();
        item.setId(String.valueOf(it.get("id")));
        item.setDoctorId(asLong(it.get("doctorId")));
        item.setRequestedByUserId(asLong(it.get("requestedByUserId")));
        item.setStatus((String) it.get("status"));
        item.setCreatedAt(asLong(it.get("createdAt")));
        item.setUpdatedAt(asLong(it.get("updatedAt")));
        item.setClinicId(asLong(it.get("clinicId")));
        item.setName((String) it.get("name"));
        item.setTitle((String) it.get("title"));
        item.setSpecialty((String) it.get("specialty"));
        item.setBio((String) it.get("bio"));
        item.setApprovedBy((String) it.get("approvedBy"));
        item.setApprovedAt(asLong(it.get("approvedAt")));
        item.setReason((String) it.get("reason"));
        return item;
    }

    private Long asLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }
}


