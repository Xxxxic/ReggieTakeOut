package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
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
    private SetmealService setmealService;

    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        // log.info(setmealDto.toString());
        setmealService.saveWithSetmealDish(setmealDto);
        return R.success("成功增加");
    }

    @GetMapping("/page")
    public R<Page<SetmealDto>> list(int page, int pageSize, String name) {
        Page<SetmealDto> pageRes = setmealService.getPageWithCategoryName(page, pageSize, name);

        return R.success(pageRes);
    }

    /**
     * 删除：单个 批量
     * DELETE "setmeal?ids=1763836166919966722,1415580119015145474"
     * 用List接收数据 这样就不用转换了
     * [1763836166919966722, 1415580119015145474]
     * TODO：停售功能
     */
    @DeleteMapping
    public R<String> delete(@RequestParam("ids") List<Long> list) {
        //log.info(list.toString());
        // 涉及两张表的删除 setmeal setmealDish
        setmealService.deleteWithSetmealDish(list);

        return R.success("删除成功");
    }


}
