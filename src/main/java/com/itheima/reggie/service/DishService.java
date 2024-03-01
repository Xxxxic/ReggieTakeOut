package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;


public interface DishService extends IService<Dish> {
    // 新增菜品 涉及两张表: 插入菜品口味 直接对DTO操作即可
    public void saveWithFlavor(DishDto dishDto);
}
