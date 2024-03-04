package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Orders;

public interface OrdersService extends IService<Orders> {

    // 提交订单：提交订单，然后将订单明细提交到OrdersDetail
    void submitWithOrderDetail(Orders orders);
}
