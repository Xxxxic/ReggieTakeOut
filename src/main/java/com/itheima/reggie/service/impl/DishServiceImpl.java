package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增菜品，同时保存对应的口味数据
     */
    @Override
    @Transactional  // 事务在发生异常时，会对数据库的操作回滚
    public void saveWithFlavor(DishDto dishDto) {
        // 保存菜品到菜品表dish
        this.save(dishDto);

        // 保存dishId到Flavor里面 形成映射
        Long id = dishDto.getId();
        // 1.fori：修改集合内部
        //for (DishFlavor flavor : dishDto.getFlavors()) {
        //    flavor.setDishId(id);
        //}
        // 2.foreach：但只可以遍历集合,不可以修改集合
        // 3.stream流：stream peek collect
        List<DishFlavor> collect = dishDto.getFlavors().stream()
                .peek((item) -> item.setDishId(id))
                .collect(Collectors.toList());

        // 保存菜品口味数据到dishFlavor
        // saveBatch批量保存：因为存放的flavor是一个集合
        dishFlavorService.saveBatch(collect);
    }

    //@Override
    //public Page<DishDto> getDishDtoPage(Page<DishDto> page, String name) {
    //    return dishMapper.getDishDtoPage(page, name);
    //}

    @Override
    public DishDto getByIdWithFlavor(Long id) {
        Dish dish = this.getById(id);

        // 根据口味id查询口味信息
        LambdaQueryWrapper<DishFlavor> qw = new LambdaQueryWrapper<>();
        qw.eq(dish.getId() != null, DishFlavor::getDishId, dish.getId());
        List<DishFlavor> list = dishFlavorService.list(qw);

        // 用DTO存储口味信息
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);
        dishDto.setFlavors(list);

        return dishDto;
    }

    @Override
    @Transactional  // 多表查询要用事务
    public void updateWithFlavor(DishDto dishDto) {
        // Dish table: 实际上传的dish 向上转型
        this.updateById(dishDto);

        Long dishId = dishDto.getId();

        // Flavor: 清理
        // 问题：创建时间失效了
        // 注意！如果口味表数据你用了逻辑删除,测试会显示用户已经存在
        LambdaQueryWrapper<DishFlavor> qw = new LambdaQueryWrapper<>();
        qw.eq(dishId != null, DishFlavor::getDishId, dishId);
        dishFlavorService.remove(qw);

        // Flavor: 重新加
        List<DishFlavor> list = dishDto.getFlavors().stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(list);
    }

}
