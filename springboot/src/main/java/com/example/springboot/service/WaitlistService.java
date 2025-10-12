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
}

