package com.example.springboot.service;

import com.example.springboot.entity.Clinic;
import com.example.springboot.mapper.ClinicMapper;
import com.example.springboot.mapper.DepartmentMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClinicService {

    @Resource
    private ClinicMapper clinicMapper;

    @Resource
    private DepartmentMapper departmentMapper;

    public List<Clinic> selectAll() {

        List<Clinic> list = clinicMapper.selectAll();

        return list;
    }

    public Clinic selectById(Long id) {
        Clinic clinic = clinicMapper.selectById(id);
        return clinic;
    }

    public int create(Clinic clinic) {
        return clinicMapper.insert(clinic);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchCreate(List<Clinic> clinics) {
        if (clinics == null || clinics.isEmpty()) {
            throw new IllegalArgumentException("门诊列表不能为空");
        }

        // 验证所有门诊是否都有departmentId
        for (Clinic clinic : clinics) {
            if (clinic.getDepartmentId() == null) {
                throw new IllegalArgumentException("门诊必须指定所属科室");
            }
            // 验证科室是否存在
            if (departmentMapper.selectById(clinic.getDepartmentId()) == null) {
                throw new IllegalArgumentException("科室ID " + clinic.getDepartmentId() + " 不存在");
            }
        }

        return clinicMapper.batchInsert(clinics);
    }

    public int update(Clinic clinic) {
        return clinicMapper.update(clinic);
    }

    public int delete(Long id) {
        return clinicMapper.delete(id);
    }
}

