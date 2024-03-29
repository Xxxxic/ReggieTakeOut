package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/sendMsg")
    //@Cacheable(key = "#user.phone", value = "code")
    // 要用缓存就要避免自调用
    public R<String> sendMsg(@RequestBody User user) {
        // 获取手机号
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)) {
            // 生成四位验证码：可以带上时间戳 一起存入redis 后面验证码校验时去掉时间戳取出原来的验证码 进行比对
            String code = ValidateCodeUtils.generateValidateCode4String(4);
            log.info("validate code: {}", code);

            // 调用阿里云发送短信验证码
            //SMSUtils.sendMessage("reggie", phone, s, "");

            // 存起来HttpSession（可以存在redis） 用于后期比对
            //session.setAttribute(phone, code);

            redisTemplate.opsForValue().set(phone, code,5, TimeUnit.MINUTES);

            return R.success(code);
        }
        return R.error("短信发送失败");
    }


    @PostMapping("/login")
    public R<String> login(@RequestBody Map<String, String> map, HttpSession session) {
        //log.info(map.toString());
        String phone = map.get("phone");
        String code = map.get("code");
        //Object codeInSession = session.getAttribute(phone);
        Object codeInSession = redisTemplate.opsForValue().get(phone);
        log.info(code);

        //进行验证码的比对（页面提交的验证码和Session中保存的验证码比对）
        if (codeInSession != null && codeInSession.equals(code)) {
            // 从数据库检索用户
            LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
            qw.eq(User::getPhone, phone);
            User user = userService.getOne(qw);
            // 没找到 - 注册
            if (user == null) {
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            // 易漏：保存用户登录状态
            session.setAttribute("user", user.getId());
            //log.info(session.getAttribute("user").toString());

            // 删除验证码
            redisTemplate.delete(phone);

            return R.error("登陆成功");
        }
        return R.error("登陆失败");
    }
}
