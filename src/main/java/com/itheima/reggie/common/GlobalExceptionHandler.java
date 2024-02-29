package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLIntegrityConstraintViolationException;

@ControllerAdvice(annotations = {RestController.class, Controller.class})
// 如果使用@ConrtollerAdvice注解的话，方法体要加@Requestbody 否则会报错
// 如果使用@RestControllerAdvice 注解 方法体不需要加@RequestBody
@ResponseBody // 把返回的数据变成json形式返回
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 异常处理方法
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.error(ex.getMessage());

        if (ex.getMessage().contains("Duplicate entry")) {
            String[] Split = ex.getMessage().split(" ");
            String msg = Split[2] + "已存在";
            return R.error(msg);
        }

        return R.error("未知错误");
    }
}
