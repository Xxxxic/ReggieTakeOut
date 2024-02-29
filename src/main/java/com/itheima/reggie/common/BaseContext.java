package com.itheima.reggie.common;

/**
 * 基于Threadlocal封装工具类，用户保存和获取当前用户id
 * 注意ThreadLocal里只能存一个东西
 * 存多个值可以new多个tl对象
 *
 * 直接在配置类里面@Bean一个ThreadLocal类?
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
