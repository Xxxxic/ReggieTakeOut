package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrdersService;
import com.itheima.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrdersController {
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private OrderDetailService orderDetailService;

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        ordersService.submitWithOrderDetail(orders);

        return R.success("提交成功");
    }

    @GetMapping("/userPage")
    public R<Page<OrdersDto>> list(int page, int pageSize) {
        Long userId = BaseContext.getCurrentId();

        Page<Orders> pageInfo = new Page(page, pageSize);
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
}
