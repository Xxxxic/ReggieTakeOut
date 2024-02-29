package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/category")
@ResponseBody
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categroyService;

    @PostMapping
    public R<String> save(@RequestBody Category category) {
        //log.info("category : {}", category);
        categroyService.save(category);
        return R.success("新增分类成功");
    }

    /**
     * 分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> get(Integer page, Integer pageSize) {
        Page<Category> pageInfo = new Page<>(page, pageSize);

        // 条件构造器
        LambdaQueryWrapper<Category> qw = new LambdaQueryWrapper<>();
        qw.orderByAsc(Category::getSort);

        categroyService.page(pageInfo, qw);

        return R.success(pageInfo);
    }

    /**
     * 根据id删除分类
     * 注意这里前端传过来的数据是ids：/category?ids=1763188430562508801
     * @param id
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam("ids") Long id){
        log.info("删除分类：{}",id);
        //categroyService.removeById(id);
        categroyService.remove(id);
        // TODO: confirm the result to return differently
        return R.success("删除成功");
    }

    /**
     * 根据id修改分类
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        //log.info(category.toString());
        categroyService.updateById(category);

        return R.success("修改成功");
    }
}
