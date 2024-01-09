package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Employee;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/category")
@Api(tags = "分类相关接口")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */
    @ApiOperation("新增分类接口")
    @PostMapping
    public Result<String> save(@RequestBody CategoryDTO categoryDTO){
        log.info("新增分类参数:{}",categoryDTO);
        categoryService.save(categoryDTO);
        return Result.success();
    }

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    @ApiOperation("分类分页查询")
    @GetMapping("/page")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("分类分页查询参数:{}",categoryPageQueryDTO);
        PageResult pageResult = categoryService.pageQuery(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据id删除分类
     * @param id
     * @return
     */
    @ApiOperation("根据id删除分类")
    @DeleteMapping
    public Result<String> deleteById(Long id){
        log.info("删除分类:{}",id);
        categoryService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改分类
     * @param categoryDTO
     * @return
     */
    @ApiOperation("修改分类")
    @PutMapping
    public Result<String> update(@RequestBody CategoryDTO categoryDTO){
        log.info("修改分类:{}",categoryDTO);
        categoryService.update(categoryDTO);
        return Result.success();
    }

    @ApiOperation("启用禁用分类")
    @PostMapping("/status/{status}")
    public Result<String> startOrStop(@PathVariable("status") Integer status, Long id){
        log.info("启用禁用分类:{},{}",status,id);
        categoryService.startOrStop(status,id);
        return Result.success();
    }

    @ApiOperation("根据类型查询分类")
    @GetMapping("/list")
    public Result<List<Category>> list(Integer type){
        log.info("根据类型查询分类:{}",type);
        List<Category> list = categoryService.list(type);
        return Result.success(list);

    }



}
