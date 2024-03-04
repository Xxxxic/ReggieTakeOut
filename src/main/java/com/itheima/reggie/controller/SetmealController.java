package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 对于Setmeal - Dish 只需要一个controller即可
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;

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

    /**
     * 根据分类信息获取套餐的数据
     * GET setmeal/list?categoryId=1413342269393674242&status=1
     * 接收参数：@RequestParam("categoryId") Long id, @RequestParam("status") int status
     * 但是选择用Setmeal接收更好
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        //log.info(id.toString());

        LambdaQueryWrapper<Setmeal> qw = new LambdaQueryWrapper<>();
        qw.eq(setmeal.getId() != null, Setmeal::getCategoryId, setmeal.getId())
                .eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        qw.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(qw);

        return R.success(list);
    }

    /**
     * 修改套餐状态：启售、停售
     * 支持批量操作
     * 停售：不影响菜品的售卖状态
     * 启售：需要查看菜品是否都启售
     */
    @PostMapping("/status/{status}")
    public R<String> update(@PathVariable Integer status, @RequestParam List<Long> ids) {
        //log.info(status.toString());
        //log.info(ids.toString());

        // 检查非法情况
        if (status != 0 && status != 1) {
            throw new CustomException("状态请求参数错误");
        }
        if (status == 1) {
            boolean flag = setmealService.checkDishStatus(ids);
            if (!flag) {
                throw new CustomException("有菜品处于停售状态！");
            }
        }

        LambdaUpdateWrapper<Setmeal> uw = new LambdaUpdateWrapper<>();
        uw.in(Setmeal::getId, ids);     // 注意这里是可以传List的 但是要用in！！！！
        uw.set(Setmeal::getStatus, status);
        setmealService.update(uw);

        return R.success("套餐状态修改成功");
    }

    @GetMapping("/{id}")
    public R<SetmealDto> getOne(@PathVariable Long id) {

        SetmealDto setmealDto = new SetmealDto();
        Setmeal one = setmealService.getById(id);
        if (one == null) {
            throw new CustomException("套餐不存在！");
        }
        BeanUtils.copyProperties(one, setmealDto);
        LambdaQueryWrapper<SetmealDish> qw = new LambdaQueryWrapper<>();
        qw.eq(setmealDto.getId() != null, SetmealDish::getSetmealId, setmealDto.getId());
        List<SetmealDish> list = setmealDishService.list(qw);
        setmealDto.setSetmealDishes(list);
        return R.success(setmealDto);
    }

    /**
     * 修改套餐
     * 先根据Setmeal_id 把setmealDish表中对应套餐的数据删了
     * 然后重新添加 调用前面写好的方法
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        // 删除
        LambdaQueryWrapper<SetmealDish> qw = new LambdaQueryWrapper<>();
        qw.eq(setmealDto.getId() != null, SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(qw);

        //更新套餐数据
        setmealService.updateById(setmealDto);

        // 保存其中的菜品关系 到SetmealDish表中
        // 先将Setmeal的Id赋值
        List<SetmealDish> list = setmealDto.getSetmealDishes().stream()
                .peek((item) -> item.setSetmealId(setmealDto.getId()))
                .collect(Collectors.toList());
        // 然后再Batch保存
        setmealDishService.saveBatch(list);

        return R.success("套餐更新成功");
    }

}
