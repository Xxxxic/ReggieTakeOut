package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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
        shoppingCart.setUserId(userId);

        LambdaQueryWrapper<ShoppingCart> qw = new LambdaQueryWrapper<>();
        // 查询当前套餐/菜品是否存在于当前购物车
        if (shoppingCart.getDishId() != null) {
            qw.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else if (shoppingCart.getSetmealId() != null) {
            qw.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        } else {
            throw new CustomException("购物车数据不存在");
        }
        ShoppingCart one = shoppingCartService.getOne(qw);
        // 不存在直接加入
        if (one == null) {
            //如果不存在，则还需设置一下创建时间
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
        } else {
            // 存在则 number++
            shoppingCart.setNumber(shoppingCart.getNumber() + 1);
            shoppingCartService.updateById(shoppingCart);
        }

        // TODO: 同一种菜无法选不同口味

        return R.success("新增成功");
    }

    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> qw = new LambdaQueryWrapper<>();
        qw.eq(userId != null, ShoppingCart::getUserId, userId);
        List<ShoppingCart> list = shoppingCartService.list(qw);

        return R.success(list);
    }

    @DeleteMapping("/clean")
    public R<String> clean() {
        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> qw = new LambdaQueryWrapper<>();
        qw.eq(userId != null, ShoppingCart::getUserId, userId);
        shoppingCartService.remove(qw);

        return R.success("清空成功");
    }

    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart) {
        //log.info(shoppingCart.toString());

        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> qw = new LambdaQueryWrapper<>();
        qw.eq(userId != null, ShoppingCart::getUserId, userId);
        if (shoppingCart.getDishId() != null) {
            qw.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else if (shoppingCart.getSetmealId() != null) {
            qw.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        } else {
            throw new CustomException("购物车数据不存在");
        }
        shoppingCartService.remove(qw);

        return R.success("删除成功");
    }
}
