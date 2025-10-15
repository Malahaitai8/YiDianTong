package com.example.springboot.controller;

import com.example.springboot.exception.CustomerException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;


@Tag(name = "测试接口", description = "系统测试相关接口")
@RestController
public class WebController {

    @Operation(summary = "Hello测试", description = "简单的测试接口")
    @GetMapping("/hello")
    public  String hello(){

        return "hello";
    }



}
