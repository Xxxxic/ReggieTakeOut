package com.itheima.reggie.common;

/**
 * 自定义业务异常类
 * RuntimeException
 */
public class CustomException extends RuntimeException{
    public CustomException(String msg){
        super(msg);
    }
}
