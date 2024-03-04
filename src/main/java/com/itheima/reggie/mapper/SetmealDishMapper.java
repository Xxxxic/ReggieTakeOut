package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SetmealDishMapper extends BaseMapper<SetmealDish> {

    // select count(d.status) from setmeal_dish sd left join dish d on sd.dish_id = d.id
    // where sd.dish_id = id and d.status = 1;
    @Select("select count(d.status) " +
            "from take_out.setmeal_dish sd left join take_out.dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmeal_id} and d.status = 0;")
    int checkDishStatus(@Param("setmeal_id") Long id);
}
