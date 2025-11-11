package com.example.springboot.service;

import com.example.springboot.entity.Waitlist;
import com.example.springboot.mapper.WaitlistMapper;
import com.example.springboot.mapper.ScheduleMapper;
import com.example.springboot.entity.Schedule;
import com.example.springboot.exception.CustomerException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WaitlistService {

    @Resource
    private WaitlistMapper waitlistMapper;

    @Resource
    private ScheduleMapper scheduleMapper;

    public List<Waitlist> selectAll() {
        List<Waitlist> list = waitlistMapper.selectAll();
        return list;
    }

    public Waitlist selectById(Long id) {
        Waitlist waitlist = waitlistMapper.selectById(id);
        return waitlist;
    }

    public int create(Waitlist waitlist) {
        return waitlistMapper.insert(waitlist);
    }

    public int update(Waitlist waitlist) {
        return waitlistMapper.update(waitlist);
    }

    public int delete(Long id) {
        return waitlistMapper.delete(id);
    }

    /** 根据患者ID查询候补列表 */
    public List<Waitlist> listByPatient(Long patientId) {
        return waitlistMapper.selectByPatientId(patientId);
    }

    /** 加入候补队列（仅在号源已满时允许，且避免重复加入） */
    public Waitlist addToQueue(Long patientId, Long scheduleId) {
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new CustomerException("排班不存在");
        }
        if (schedule.getAvailableSlots() != null && schedule.getAvailableSlots() > 0) {
            throw new CustomerException("当前仍有号源，可直接预约");
        }
        int exists = waitlistMapper.existsByPatientAndSchedule(patientId, scheduleId);
        if (exists > 0) {
            throw new CustomerException("已在候补队列中，请勿重复提交");
        }

        Waitlist w = new Waitlist();
        w.setPatientId(patientId);
        w.setScheduleId(scheduleId);
        w.setJoinTime(new java.util.Date());
        w.setStatus("WAITING");
        waitlistMapper.insert(w);
        return w;
    }

    /** 弹出队首并返回，若无则返回 null */
    public Waitlist popNext(Long scheduleId) {
        Waitlist next = waitlistMapper.selectNextWaiting(scheduleId);
        if (next != null) {
            waitlistMapper.updateStatus(next.getId(), "GRANTED");
        }
        return next;
    }
}

 