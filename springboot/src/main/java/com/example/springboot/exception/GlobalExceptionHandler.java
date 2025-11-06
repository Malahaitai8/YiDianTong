package com.example.springboot.exception;

import com.example.springboot.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


@ControllerAdvice("com.example.springboot.controller")
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    @ResponseBody   // 返回json
    public Result handleException(Exception e){
        logger.error("捕获到全局异常: {}", e.getMessage(), e);
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(CustomerException.class)
    @ResponseBody
    public Result handleCustomerException(CustomerException e){
        logger.warn("业务异常: {}", e.getMsg());
        return new Result(e.getCode(), e.getMsg(), null);
    }

}
