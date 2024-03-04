package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealDishMapper;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal>
        implements SetmealService {
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;

    @Override
    public void saveWithSetmealDish(SetmealDto setmealDto) {
        // 保存到Setmeal表中
        this.save(setmealDto);

        // 保存其中的菜品关系 到SetmealDish表中
        // 先将Setmeal的Id赋值
        List<SetmealDish> list = setmealDto.getSetmealDishes().stream()
                .peek((item) -> item.setSetmealId(setmealDto.getId()))
                .collect(Collectors.toList());
        // 然后再Batch保存
        setmealDishService.saveBatch(list);
    }

    @Override
    public Page<SetmealDto> getPageWithCategoryName(int page, int pageSize, String name) {
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        // 名字查询
        LambdaQueryWrapper<Setmeal> qw = new LambdaQueryWrapper<>();
        qw.like(name != null, Setmeal::getName, name);
        qw.orderByDesc(Setmeal::getUpdateTime);

        this.page(pageInfo, qw);

        // 此时查出来的有CategoryID，但是前端需要CategoryName，所以还需要再查一次
        Page<SetmealDto> pageRes = new Page<>(page, pageSize);
        BeanUtils.copyProperties(pageInfo, pageRes, "records");

        List<SetmealDto> list = pageInfo.getRecords().stream().map((item) -> {
            SetmealDto setmealDtoTemp = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDtoTemp);
            // 赋值分类名
            Category category = categoryService.getById(item.getCategoryId());
            // 好习惯：每次查出来东西 就判断为不为空 避免空指针
            if (category != null) {
                setmealDtoTemp.setCategoryName(category.getName());
            }
            return setmealDtoTemp;
        }).collect(Collectors.toList());

        pageRes.setRecords(list);

        return pageRes;
    }

    /**
     * 删除套餐 & 关系
     * TODO: 本地的图片也需要删除
     */
    @Override
    public void deleteWithSetmealDish(List<Long> list) {
        // 先查询套餐状态 启售时不能删除
        // select count(*) from setmeal where id in ids and status = 1;
        LambdaQueryWrapper<Setmeal> qw1 = new LambdaQueryWrapper<>();
        qw1.in(Setmeal::getId, list).eq(Setmeal::getStatus, 1);
        if (this.count(qw1) > 0) {
            throw new CustomException("套餐正启售中，无法删除");
        }

        // 在setmeal处删除
        this.removeByIds(list);

        // 在setmealDish 根据SetmealId来删除对应的关系
        LambdaQueryWrapper<SetmealDish> qw2 = new LambdaQueryWrapper<>();
        // 参数可以直接用list吗？？ --- 不能，需要使用in
        qw2.in(SetmealDish::getSetmealId, list);
        setmealDishService.remove(qw2);
    }

    @Override
    public boolean checkDishStatus(List<Long> list) {
        // 根据SetmealId检查 菜品启售状态
        for (Long id : list) {
            int num = setmealDishMapper.checkDishStatus(id);
            //log.info("查询停用菜品数: {}", String.valueOf(num));
            if(num > 0){
                return false;
            }
        }
        return true;
    }


}
