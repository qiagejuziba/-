package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/admin/dish")
@RestController
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 新增菜品
     *
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品:{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @ApiOperation("菜品分页查询")
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询:{}", dishPageQueryDTO);
        PageResult pageResult = dishService.page(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据id批量删除菜品和对应口味
     *
     * @param ids
     * @return
     */
    @ApiOperation("根据id批量删除菜品和对应口味")
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids) {
        log.info("根据id批量删除菜品和对应口味:{}", ids);
        dishService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 根据id查询菜品
     *
     * @param id
     * @return
     */
    @ApiOperation("根据id查询菜品")
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询菜品:{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);

    }


    /**
     * 修改菜品
     *
     * @param dishDTO
     * @return
     */
    @ApiOperation("修改菜品")
    @PutMapping
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品:{}", dishDTO);
        dishService.updateDishWithFlavor(dishDTO);
        return Result.success();
    }

    /**
     * 启用禁用菜品
     * @param status
     * @param id
     * @return
     */
    @ApiOperation("启用禁用菜品")
    @PostMapping("status/{status}")
    public Result startOrStop(@PathVariable("status") Integer status, Long id) {
        log.info("启用禁用菜品:{},{}",status,id);
        dishService.startOrStop(status,id);
        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @ApiOperation("根据分类id查询菜品")
    @GetMapping("/list")
    public Result<List<Dish>> list(Long categoryId) {
        log.info("根据分类id查询菜品:{}", categoryId);
        List<Dish> dishList = dishService.list(categoryId);
        return Result.success(dishList);
    }


}
