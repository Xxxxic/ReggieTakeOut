package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;


public interface DishService extends IService<Dish> {
    // 新增菜品 涉及两张表: 插入菜品口味 直接对DTO操作即可
    void saveWithFlavor(DishDto dishDto);

    // 分页查询 涉及两张表：菜品分页 查出对应分类名称
    // Page<DishDto> getDishDtoPage(Page<DishDto> p, String name);

    // 获取dishDTO Dish+Flavor
    DishDto getByIdWithFlavor(Long id);

    // 联合Flavor表一起更新
    void updateWithFlavor(DishDto dishDto);
}
