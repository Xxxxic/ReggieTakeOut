package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
//@ResponseBody
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

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

    /**
     * 菜品dish的分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        // 分页器对象
        // 直接对dish进行分页查询，查询出来的数据是没有categoryName的
        Page<Dish> pageDish = new Page<>(page, pageSize);
        Page<DishDto> pageDishDto = new Page<>(page, pageSize);

        // 查询条件
        LambdaQueryWrapper<Dish> qw = new LambdaQueryWrapper<>();
        qw.like(name != null, Dish::getName, name);
        qw.orderByDesc(Dish::getUpdateTime);

        dishService.page(pageDish, qw);

        //对象拷贝  使用框架自带的工具类，第三个参数是不拷贝到属性
        BeanUtils.copyProperties(pageDish, pageDishDto, "records");
        // getRecords获取到dish的所有数据 records属性是分页插件中表示分页中所有的数据的一个集合
        List<DishDto> dishDtoList = pageDish.getRecords().stream().map((item) -> {
            DishDto dishDtoItem = new DishDto();
            BeanUtils.copyProperties(item, dishDtoItem);

            Long Id = item.getCategoryId();
            Category category = categoryService.getById(Id);
            if (category != null) {
                dishDtoItem.setCategoryName(category.getName());
            }
            return dishDtoItem;
        }).collect(Collectors.toList());
        pageDishDto.setRecords(dishDtoList);

        // TODO: 优化联表查询
        // Page<DishDto> resultPage = dishService.getDishDtoPage(resPage, name);

        return R.success(pageDishDto);
    }

    /**
     * 根据菜品id查询菜品信息
     * 用于回显
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    /**
     * 修改菜品：也修改口味表
     *
     * @return
     * @Param DishDto
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        //dishService.saveWithFlavor(dishDto);
        dishService.updateWithFlavor(dishDto);

        return R.success("菜品修改成功");
    }

}
