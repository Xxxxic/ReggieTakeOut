package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
    @Select("select d.*, c.name as categoryName " +
            "from take_out.dish d left join take_out.category c on d.category_id = c.id " +
            "where d.name like CONCAT('%', #{name}, '%')")
    Page<DishDto> getDishDtoPage(Page<DishDto> page, @Param("name") String name);
}
