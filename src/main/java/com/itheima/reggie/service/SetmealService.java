package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    void saveWithSetmealDish(SetmealDto setmealDto);

    // 带分类名的分页查询
    Page<SetmealDto> getPageWithCategoryName(int page, int pageSize, String name);

    // 删除套餐 同时删除关联关系
    void deleteWithSetmealDish(List<Long> list);

    // 检查套餐中的菜品是否都为启售状态
    boolean checkDishStatus(List<Long> list);
}
