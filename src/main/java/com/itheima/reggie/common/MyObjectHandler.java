package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自定义元数据对象处理器
 */
@Slf4j
@Component  //注意:这个要记得交给spring容器管理
// 怎么确定你要添加的功能是不是要交给容器管理呢？
// 就是你直接写了一个工具类或者是功能类，需要对数据库的数据或者是数据库数据的结果产生影响的时候
// 明明写了这样一个类，但是功能却没有生效，那么这个时候就要首先考虑是不是容器没有托管这个类
public class MyObjectHandler implements MetaObjectHandler {
    /**
     * 插入操作，自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("公共字段自动填充[insert]...");
        log.info(metaObject.toString());

        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        // 其实也可以在这个自动填充类中使用@Autowired注入session对象，一样可以获得id
        // 获得session的原理和后面讲的ThreadLocal在线程中存储线程变量的原理应该是差不多的
        // 获取的session是个代理对象，会根据当前线程中存储的内容获得当前请求的上下文信息，从而获取session对象
        // @AutoWire方式获取session本质上是通过RequestContextHolder获取的，而RequestContextHolder的获取本质上应该也是基于线程变量的
        //metaObject.setValue("createUser", 1L);
        //metaObject.setValue("updateUser", 1L);
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
        metaObject.setValue("createUser", BaseContext.getCurrentId());
    }

    /**
     * 更新操作，自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段自动填充[update]...");
        log.info(metaObject.toString());

        metaObject.setValue("updateTime", LocalDateTime.now());
        //metaObject.setValue("updateUser", 1L);
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }
}
