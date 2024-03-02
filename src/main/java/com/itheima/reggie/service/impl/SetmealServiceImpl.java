package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal>
        implements SetmealService {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    public void saveWithSetmealDish(SetmealDto setmealDto) {
        // 保存到Setmeal表中
        setmealService.save(setmealDto);

        // 保存其中的菜品关系 到SetmealDish表中
        // 先将Setmeal的Id赋值
        List<SetmealDish> list = setmealDto.getSetmealDishes().stream()
                .peek((item) -> item.setSetmealId(setmealDto.getId()))
                .collect(Collectors.toList());
        // 然后再Batch保存
        setmealDishService.saveBatch(list);
    }
}
