package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.entity.Waitlist;
import com.example.springboot.service.WaitlistService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/waitlist")
public class WaitlistController {

    @Resource
    private WaitlistService waitlistService;


    @GetMapping("/selectAll")
    public Result selectAll() {

        List<Waitlist> list = waitlistService.selectAll();

        return Result.success(list);
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Long id) {

        Waitlist waitlist = waitlistService.selectById(id);
        return Result.success(waitlist);
    }

}

