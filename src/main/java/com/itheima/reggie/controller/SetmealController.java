package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 对于Setmeal - Dish 只需要一个controoler即可
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        // log.info(setmealDto.toString());
        setmealService.saveWithSetmealDish(setmealDto);
        return R.success("成功增加");
    }

    @GetMapping("/page")
    public R<Page<SetmealDto>> list(int page, int pageSize, String name) {
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        // 名字查询
        LambdaQueryWrapper<Setmeal> qw = new LambdaQueryWrapper<>();
        qw.like(name != null, Setmeal::getName, name);
        qw.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo, qw);

        // 此时查出来的有CategoryID，但是前端需要CategoryName，所以还需要再查一次
        Page<SetmealDto> pageRes = new Page<>(page, pageSize);
        BeanUtils.copyProperties(pageInfo, pageRes, "records");

        List<SetmealDto> list = pageInfo.getRecords().stream().map((item) -> {
            SetmealDto setmealDtoTemp = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDtoTemp);
            // 赋值分类名
            Category category = categoryService.getById(item.getCategoryId());
            // 好习惯：每次查出来东西 就判断为不为空 避免空指针
            if (category != null){
                setmealDtoTemp.setCategoryName(category.getName());
            }
            return setmealDtoTemp;
        }).collect(Collectors.toList());

        pageRes.setRecords(list);

        return R.success(pageRes);
    }


}
