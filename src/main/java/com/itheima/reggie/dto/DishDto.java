package com.itheima.reggie.dto;

import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {
    // 属性无论是私有还是公共的都是可以继承的，只不过私有的属性不能用而已
    private List<DishFlavor> flavors = new ArrayList<>();

    private String CategoryName;

    private Integer copies;
}
