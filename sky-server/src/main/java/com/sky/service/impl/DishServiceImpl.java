package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    /**
     * 新增菜品和对应口味
     * @param dishDTO
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        //1.新增一个菜品
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

        dishMapper.insert(dish);
        // 2.3获取dishId
        Long dishId = dish.getId();
        //2.新增n个口味
        //2.1获取口味集合数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0){
            //2.因为DishFlavor中有dishId，但是dishId只有插入以后才会有，所以我们需要利用主键回显，获取dishId
            // 遍历集合，设置dishId
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            //2.2向口味表中插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }


    }
}
