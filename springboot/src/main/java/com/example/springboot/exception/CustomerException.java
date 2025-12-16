package com.example.springboot.exception;

public class CustomerException extends RuntimeException{
    private String code;
    private String msg;

    public CustomerException(String code, String msg) {
        super(msg);  // 调用父类构造函数，设置 message
        this.code = code;
        this.msg = msg;
    }

    public CustomerException(String msg) {
        super(msg);  // 调用父类构造函数，设置 message
        this.code = "500";
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
