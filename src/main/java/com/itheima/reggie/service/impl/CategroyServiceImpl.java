package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategroyServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除分类，删除之前进行外键约束的判断
     * dish & setmeal 表中有category的id
     */
    public void remove(Long id) {
        // 查询是否关联了相应菜品 - 抛异常
        LambdaQueryWrapper<Dish> qw1 = new LambdaQueryWrapper<>();
        qw1.eq(Dish::getCategoryId, id);
        // 注意:这里使用count方法的时候一定要传入条件查询的对象，否则计数计算出来的是全部的数据的条数
        int count1 = dishService.count(qw1);
        if (count1 > 0) {
            throw new CustomException("当前分类项关联了菜品,不能删除");
        }

        // 查询是否关联了相应套餐 - 抛异常
        LambdaQueryWrapper<Setmeal> qw2 = new LambdaQueryWrapper<>();
        qw2.eq(Setmeal::getCategoryId, id);
        int count2 = setmealService.count(qw2);
        if (count2 > 0) {
            throw new CustomException("当前分类项关联了套餐,不能删除");
        }

        // 正常删除
        super.removeById(id);
    }

}
