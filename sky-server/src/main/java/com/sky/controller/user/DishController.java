package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController("userDishController")
@RequestMapping("/user/dish")
@Api(tags = "C端菜品浏览相关接口")
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 根据菜品分类id查询菜品以及相关口味
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据菜品分类id查询菜品以及相关口味")
    public Result<List<DishVO>> list(Long categoryId){
        log.info("根据菜品分类id查询菜品以及相关口味:{}",categoryId);
        //只查询起售的菜品，所以设置菜品状态
        Dish dish =  new Dish();
        dish.setStatus(StatusConstant.ENABLE);
        dish.setCategoryId(categoryId);
        //查询菜品
        List<DishVO> dishVOList =  dishService.listWithFlavor(dish);
        return Result.success(dishVOList);
    }



}
