package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dish")
//@ResponseBody
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 添加菜品
     * 钱数以分为单位：为了防止出现小数而导致精度丢失
     *
     * @return
     * @Param DishDto
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        //log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("菜品添加成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        // 构造分页器对象
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Dish> qw = new LambdaQueryWrapper<>();
        // 过滤条件
        qw.like(name != null, Dish::getName, name);
        qw.orderByDesc(Dish::getUpdateTime);

        dishService.page(pageInfo, qw);

        // TODO: 问题是没有返回categoryName 需要再查表赋值


        return R.success(pageInfo);
    }

}
