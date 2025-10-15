package com.example.springboot.service;

import com.example.springboot.entity.Waitlist;
import com.example.springboot.mapper.WaitlistMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WaitlistService {

    @Resource
    private WaitlistMapper waitlistMapper;

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

    /** 加入候补队列 */
    public Waitlist addToQueue(Long patientId, Long scheduleId) {
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

