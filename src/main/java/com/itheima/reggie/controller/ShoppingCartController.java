package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    /**
     * 添加到购物车
     * 不用 HttpServletRequest servletRequest
     * 直接去BaseContext取UserId
     * POST http://localhost/shoppingCart/add
     *
     * @param shoppingCart 某个菜品
     * @return 返回信息
     */
    @PostMapping("/add")
    public R<String> save(@RequestBody ShoppingCart shoppingCart) {
        log.info(shoppingCart.toString());

        // 用userid确定哪个用户的购物车
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setId(userId);

        LambdaQueryWrapper<ShoppingCart> qw = new LambdaQueryWrapper<>();
        // 查询当前套餐/菜品是否存在于当前购物车
        if (shoppingCart.getDishId() != null) {
            qw.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else if (shoppingCart.getSetmealId() != null) {
            qw.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart one = shoppingCartService.getOne(qw);
        // 不存在直接加入
        if (one == null) {
            //如果不存在，则还需设置一下创建时间
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
        }
        // 存在则 number++
        shoppingCart.setNumber(shoppingCart.getNumber() + 1);
        shoppingCartService.updateById(shoppingCart);

        // TODO: 同一种菜无法选不同口味

        return R.success("新增成功");
    }
}
