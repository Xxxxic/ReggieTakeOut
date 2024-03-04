package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrdersMapper;
import com.itheima.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders>
        implements OrdersService {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private UserService userService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 提交订单：提交订单，然后将订单明细提交到OrdersDetail
     *
     */
    @Override
    public void submitWithOrderDetail(Orders orders){
        // Orders(id=null, number=null, status=null, userId=null, addressBookId=1763914545476190209, orderTime=null, checkoutTime=null,
        // payMethod=1, amount=null, remark=, userName=null, phone=null, address=null, consignee=null)
        log.info(orders.toString());

        //获取当前用户id
        Long userId = BaseContext.getCurrentId();
        //根据用户id查询其购物车数据
        LambdaQueryWrapper<ShoppingCart> qw = new LambdaQueryWrapper<>();
        qw.eq(userId!=null,ShoppingCart::getUserId, userId);
        List<ShoppingCart> listShoppingCart = shoppingCartService.list(qw);

        // 检查无法下单的情况：购物车 或 地址为空
        if(shoppingCartService==null){
            throw new CustomException("购物车为空，无法下单");
        }
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if (addressBookId == null) {
            throw new CustomException("地址信息有误，不能下单");
        }

        // 获取信息：对订单信息进行赋值
        User user = userService.getById(userId);
        long orderId = IdWorker.getId();    // ？
        AtomicInteger amount = new AtomicInteger();
        // 填充订单细节信息
        List<OrderDetail> orderDetailList = listShoppingCart.stream().map((item)->{
            OrderDetail temp = new OrderDetail();
            temp.setOrderId(orderId);
            temp.setName(item.getName());
            temp.setImage(item.getImage());
            temp.setDishId(item.getDishId());
            temp.setSetmealId(item.getSetmealId());
            temp.setDishFlavor(item.getDishFlavor());
            // 然后计算价格
            temp.setNumber(item.getNumber());
            temp.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());

            return temp;
        }).collect(Collectors.toList());
        // 填充订单
        orders.setId(orderId);
        orders.setNumber(String.valueOf(orderId));  // 订单号 = OrderId
        orders.setUserId(userId);
        orders.setUserName(user.getName());
        orders.setPhone(addressBook.getPhone());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));
        orders.setConsignee(addressBook.getConsignee());
        orders.setAddressBookId(addressBookId);
        orders.setAddress(
                (addressBook.getProvinceName() == null ? "":addressBook.getProvinceName())+
                        (addressBook.getCityName() == null ? "":addressBook.getCityName())+
                        (addressBook.getDistrictName() == null ? "":addressBook.getDistrictName())+
                        (addressBook.getDetail() == null ? "":addressBook.getDetail())
        );
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());

        //根据查询到的购物车数据，对订单表插入数据（1条）
        this.save(orders);

        //根据查询到的购物车数据，对订单明细表插入数据（多条）
        orderDetailService.saveBatch(orderDetailList);

        //清空购物车数据
        shoppingCartService.remove(qw);
    }
}
