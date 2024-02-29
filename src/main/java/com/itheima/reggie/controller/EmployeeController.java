package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登陆
     *
     * @param request  为什么不用HttpSession？jwt?
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        // 1.加密 md5
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 2.查询数据库
        LambdaQueryWrapper<Employee> qw = new LambdaQueryWrapper<>();
        // 方法引用：Employee::getUsername创建一个Employee对象并调用其getUsername方法
        qw.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(qw);

        // 3.没查到
        if (emp == null) {
            return R.error("用户名不存在");
        }

        // 4.查到则对比 - 不一致返回失败
        if (!emp.getPassword().equals(password)) {
            return R.error("密码不正确");
        }

        // 5.一致 再查看状态 - 禁用则返回禁用结果
        if (emp.getStatus() == 0) {
            return R.error("禁用状态");
        }

        // 6.成功 id存入Session 返回成功
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }

    /**
     * 员工退出
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        // 1.清除Session中的用户id
        request.getSession().removeAttribute("employee");

        // 2.返回结果
        return R.success("退出成功");
    }

    /**
     * 新增员工
     *
     * @param request
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("新增员工，员工信息：{}", employee.toString());

        // 初始密码,md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        // status入库自动赋值

        // time
        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());

        // 创建者更新者_id 强转为Long
        // 转Long报错的话，先用对象Integer接，再转Long
        Long empId = (Long) request.getSession().getAttribute("employee");
        //employee.setCreateUser(empId);
        //employee.setUpdateUser(empId);

        log.info(employee.toString());
        employeeService.save(employee);

        return R.success("添加成功");
    }

    /**
     * 员工分页查询
     * 前端请求：Get "/employee/page?page=1&pageSize=10"
     * 这三个参数为啥不用Request注解就能传过来: 传过来的不是json格式，直接就是键值对 并且名字和形参是一样的
     * Requestbody 是加在对象上的，如果是json格式就加，此处如果是page对象就要加
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //log.info("page = {}, pageSize = {}. name = {}", page, pageSize, name);

        // 分页构造器
        Page pageInfo = new Page(page, pageSize);
        // 条件构造器 动态封装 前端传过来的过滤条件  记得加泛型
        LambdaQueryWrapper<Employee> qw = new LambdaQueryWrapper();
        // 这里注意StringUtils 导commons包
        // 条件查询 这里的条件是不为空
        qw.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        // 排序条件
        qw.orderByDesc(Employee::getUpdateTime);
        // 查询 mybatis-plus帮我们封装好了
        employeeService.page(pageInfo, qw);

        return R.success(pageInfo);
    }


    /**
     * 编辑用户
     * PUT /employee {id: 1763010259363885000, status: 0}
     * 可以复用到 禁用员工
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        log.info(employee.toString());

        // 设置更新时间和用户 用httpSeesion范围过大 浪费了资源
        //employee.setUpdateTime(LocalDateTime.now());
        //employee.setUpdateUser((Long) request.getSession().getAttribute("employee"));

        // 注意这里 js只有16位精度，而雪花19位：会导致ID精度丢失 出问题
        // 法一: 在服务端给页面响应json数据时进行处理，将long型数据统一转为String字符串
        // 法二：entity中的id上方加注解  @JsonSerialize(using = ToStringSerializer.class)
        // 法三：关闭mp雪花，用自增ID
        employeeService.updateById(employee);

        return R.success("修改成功");
    }

    /**
     * 根据id 查询员工信息
     * 因为路径用的是Restful风格，并且id在路径上传的，所以要用这个注解接收一下
     * 同名参数可以不加
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        log.info("根据id:{} 查询员工信息", id.toString());

        Employee employee = employeeService.getById(id);
        // 判断是否员工存在
        if(employee != null){
            return R.success(employee);
        }

        return R.error("无对应员工");
    }

}
