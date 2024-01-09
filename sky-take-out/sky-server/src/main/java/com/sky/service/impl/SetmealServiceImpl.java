package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 套餐业务的实现
 */
@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     *
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        //转换为Setmeal实体类对象
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //插入套餐信息
        setmealMapper.insert(setmeal);

        //获取生成套餐的id
        Long setmealId = setmeal.getId();
        //获取套餐包含的菜品信息
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //遍历套餐中的菜品，设置id
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });
        //批量保存套餐和菜品的关联关系
        setmealDishMapper.insertBatch(setmealDishes);

    }

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        //创建分页条件
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        //获取分页结果
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除套餐
     *
     * @param ids
     */
    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        //起售中的套餐不能删除
        ids.forEach(id -> {
            //根据id查询套餐信息
            Setmeal setmeal = setmealMapper.getById(id);
            //判断要删除的套餐状态，如果是起售则抛出异常
            if (StatusConstant.ENABLE == setmeal.getStatus()) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });
        //删除套餐
        ids.forEach(setmealId -> {
            //删除套餐表中的数据
            setmealMapper.deleteByIds(setmealId);
            //删除套餐菜品关系表中的数据
            setmealDishMapper.deleteBySetmealId(setmealId);
        });
    }

    /**
     * 根据id查询套餐和套餐菜品关系信息，用于修改页面回显
     *
     * @param id
     * @return
     */
    @Override
    public SetmealVO getByIdWithDish(Long id) {
        //获取套餐信息
        Setmeal setmeal = setmealMapper.getById(id);
        //获取套餐菜品关联信息
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
        SetmealVO setmealVO = new SetmealVO();
        //拷贝
        BeanUtils.copyProperties(setmeal, setmealVO);
        //设置套餐菜品关系信息
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 修改套餐信息
     *
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        //属性拷贝---Setmeal类去接受
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //1.修改套餐表，执行update
        setmealMapper.update(setmeal);

        //获取当前套餐id
        Long setmealId = setmealDTO.getId();
        //把原来的套餐菜品关系直接删除再重新插入前端传过来的数据
        setmealDishMapper.deleteBySetmealId(setmealId);
        //获取当前套餐和菜品关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            //设置套餐id
            setmealDish.setSetmealId(setmealId);
        });
        //重新插入套餐和菜品的关联关系，操作setmeal_dish表，执行insert
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 起售停售套餐信息
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {

        //起售套餐时，如果套餐内包含停售的菜品，则不能起售，
        if (status == StatusConstant.ENABLE) {

            List<Dish> dishList =  dishMapper.getBySetmealId(id);
            //判断菜品是否为空
            if (dishList !=null && dishList.size() > 0) {
                dishList.forEach(dish -> {
                    //起售套餐中含有停售的菜品则不能起售
                    if (dish.getStatus() == StatusConstant.DISABLE){
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }
        //Builder构建对象属性
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }

    /**
     * C端条件查询，根据分类id查询套餐
     * @param setmeal
     * @return
     */
    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> setmealList =  setmealMapper.list(setmeal);
        return setmealList;
    }

    /**
     * 根据套餐id查询菜品
     * @param id
     * @return
     */
    @Override
    public List<DishItemVO> getDishItemById(Long id) {

        return setmealDishMapper.getDishItemById(id);

    }
}
