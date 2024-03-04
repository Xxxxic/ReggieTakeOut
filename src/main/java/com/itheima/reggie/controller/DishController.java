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
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        //log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("菜品添加成功");
    }

    /**
     * 菜品dish的分页查询
     */
    @GetMapping("/page")
    public R<Page<DishDto>> page(int page, int pageSize, String name) {
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
     */
    @GetMapping("/{id}")
    public R<DishDto> getByDishId(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    /**
     * 根据分类id 查询对应 菜品list
     * Get /dish/list?categoryId=1397844263642378242
     * 用Long来接收：@RequestParam("categoryId") Long id
     * 用Dish来接受：通用性更好
     * 这时就不用加RequestBody或者RequestParam了：@RequestBody(required = false)
     * 因为get请求没有携带请求体
     */
    @GetMapping("/list")
    public R<List<DishDto>> getByCategoryId(Dish dish) {
        LambdaQueryWrapper<Dish> qw = new LambdaQueryWrapper<>();
        // 根据分类id
        qw.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        // 注意启售状态需要为1
        qw.eq(Dish::getStatus, 1);
        // 注意排序
        qw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(qw);

        // 添加口味信息
        List<DishDto> listRes = list.stream().map((item) -> {
            // 赋值Flavors: 复用代码
            return dishService.getByIdWithFlavor(item.getId());
        }).collect(Collectors.toList());

        return R.success(listRes);
    }


    /**
     * 修改菜品：也修改口味表
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        //dishService.saveWithFlavor(dishDto);
        dishService.updateWithFlavor(dishDto);

        return R.success("菜品修改成功");
    }

}
