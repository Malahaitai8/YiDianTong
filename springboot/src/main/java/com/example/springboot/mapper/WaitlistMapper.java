package com.example.springboot.mapper;

import com.example.springboot.entity.Waitlist;

import java.util.List;

public interface WaitlistMapper {
    List<Waitlist> selectAll();

    Waitlist selectById(Long id);
}

