package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrdersService;
import com.itheima.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrdersController {
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private UserService userService;


    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        ordersService.submitWithOrderDetail(orders);

        return R.success("提交成功");
    }

    @GetMapping("/userPage")
    public R<Page<OrdersDto>> list(int page, int pageSize) {
        Long userId = BaseContext.getCurrentId();

        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> pageInfoDto = new Page<>(page, pageSize);

        LambdaQueryWrapper<Orders> qw = new LambdaQueryWrapper<>();
        qw.eq(userId != null, Orders::getUserId, userId);
        qw.orderByDesc(Orders::getOrderTime);
        ordersService.page(pageInfo, qw);

        BeanUtils.copyProperties(page, pageInfo, "records");
        pageInfoDto.setRecords(pageInfo.getRecords().stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);
            // DTO里面的其他用户信息不赋值了：Orders里面有
            // 赋值
            LambdaQueryWrapper<OrderDetail> OrderDetailqw = new LambdaQueryWrapper<>();
            OrderDetailqw.eq(item.getId() != null, OrderDetail::getOrderId, item.getId());
            List<OrderDetail> list = orderDetailService.list(OrderDetailqw);

            //log.info(list.toString());

            ordersDto.setOrderDetails(list);
            return ordersDto;
        }).collect(Collectors.toList()));

        return R.success(pageInfoDto);
    }

    /**
     * 查询订单Page
     * 这里注意输入的number不能为其他类型，否则会报错
     */
    @GetMapping("/page")
    public R<Page<OrdersDto>> list(int page, int pageSize, Long number, String beginTime, String endTime) {
        //log.info(String.valueOf(number));
        //log.info(beginTime);
        //log.info(endTime);

        // 查询所有订单，时间降序排列，然后注意可能传入的参数
        Page<Orders> pageInfo = new Page<>(page, pageSize);

        LambdaQueryWrapper<Orders> qw = new LambdaQueryWrapper<>();
        qw.eq(number != null, Orders::getNumber, number);
        //时间段，大于开始，小于结束
        qw.ge(beginTime != null, Orders::getOrderTime, beginTime)
                .lt(endTime != null, Orders::getOrderTime, endTime);
        qw.orderByDesc(Orders::getOrderTime);
        ordersService.page(pageInfo, qw);

        // 检查后发现用户名UserName没有，所以需要引入Dto
        // TODO: ? 添加后发现本来就是没有的？？
        Page<OrdersDto> ordersDtoPage = new Page<>(page, pageSize);
        BeanUtils.copyProperties(pageInfo, ordersDtoPage, "records");
        ordersDtoPage.setRecords(pageInfo.getRecords().stream().map((item) -> {
            OrdersDto temp = new OrdersDto();
            BeanUtils.copyProperties(item, temp);

            String name = userService.getById(item.getUserId()).getName();
            //log.info(name);
            temp.setUserName(name);
            return temp;
        }).collect(Collectors.toList()));

        return R.success(ordersDtoPage);
    }

    /**
     * 更改订单状态
     * 注意参数
     */
    @PutMapping
    public R<String> update(@RequestBody Map<String, String> map) {
        int status = Integer.parseInt(map.get("status"));
        Long id = Long.valueOf(map.get("id"));

        // 这里不是查询wrapaper了
        LambdaUpdateWrapper<Orders> qw =new LambdaUpdateWrapper<>();
        qw.eq(Orders::getId, id);
        qw.set(Orders::getStatus, status);
        ordersService.update(qw);

        return R.success("修改订单状态成功");
    }

}
