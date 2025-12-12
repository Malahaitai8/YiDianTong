package com.example.springboot.service;

import com.example.springboot.entity.Department;
import com.example.springboot.entity.Doctor;
import com.example.springboot.mapper.DepartmentMapper;
import com.example.springboot.mapper.DoctorMapper;
import com.example.springboot.mapper.ScheduleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DepartmentService {

    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private DoctorMapper doctorMapper;

    @Resource
    private ScheduleMapper scheduleMapper;

    public List<Department> selectAll() {

        List<Department> list = departmentMapper.selectAll();

        return list;
    }

    public Department selectById(Long id) {
        Department department = departmentMapper.selectById(id);
        return department;
    }

    public int create(Department department) {
        // 校验科室描述字数限制
        if (department.getDescription() != null && department.getDescription().length() > 200) {
            throw new IllegalArgumentException("科室描述长度不能超过200个字符");
        }
        return departmentMapper.insert(department);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchCreate(List<Department> departments) {
        if (departments == null || departments.isEmpty()) {
            throw new IllegalArgumentException("科室列表不能为空");
        }
        // 校验每个科室的描述字数限制
        for (Department department : departments) {
            if (department.getDescription() != null && department.getDescription().length() > 200) {
                throw new IllegalArgumentException("科室描述长度不能超过200个字符");
            }
        }
        return departmentMapper.batchInsert(departments);
    }

    public int update(Department department) {
        // 校验科室描述字数限制
        if (department.getDescription() != null && department.getDescription().length() > 200) {
            throw new IllegalArgumentException("科室描述长度不能超过200个字符");
        }
        return departmentMapper.update(department);
    }

    public int delete(Long id) {
        return departmentMapper.delete(id);
    }

    public List<Doctor> getDoctorsByDepartment(Long departmentId) {
        List<Doctor> doctors = doctorMapper.selectByDepartmentId(departmentId);
        markTodayDuty(doctors);
        return doctors;
    }

    private void markTodayDuty(List<Doctor> doctors) {
        if (doctors == null || doctors.isEmpty()) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfDay = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date endOfDay = calendar.getTime();

        for (Doctor doctor : doctors) {
            if (doctor == null || doctor.getId() == null) {
                continue;
            }
            int count = scheduleMapper.countByDoctorAndDateRange(doctor.getId(), startOfDay, endOfDay);
            doctor.setOnDutyToday(count > 0);
        }
    }
}

