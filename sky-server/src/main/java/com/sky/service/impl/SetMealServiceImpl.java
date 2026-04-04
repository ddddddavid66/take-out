package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetMealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetMealServiceImpl implements SetMealService {

    @Autowired
    private SetMealMapper setmealMapper;
    @Autowired
    private SetMealDishMapper setMealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    @Override
    public void saveWithDish(SetmealDTO setmealDTO) {
        //1复制可以复制的信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.insert(setmeal);
        //2 创建套餐与菜品之间关系
        //前端不知道跟哪个套餐之间创建联系 所以先获取主键ID
        Long MealId = setmeal.getId();
        List<SetmealDish> setMealDishes = setmealDTO.getSetmealDishes(); //获取套餐
        //创建联系
        if(setMealDishes.isEmpty() || setMealDishes.size() == 0){
            return;
        }
        for (SetmealDish setMealDish : setMealDishes) {
            setMealDish.setSetmealId(MealId);
            Long dishId = dishMapper.getDishIdByName(setMealDish.getName());
            setMealDish.setDishId(dishId);
        }
        setMealDishMapper.insertBatch(setMealDishes);
    }

    /**
     * 分页查询实现
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<Setmeal> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        Long total = page.getTotal();
        List<Setmeal> list = page.getResult();
        return new PageResult(total,list);
    }

    /**
     * 删除套餐
     * @param ids
     */
    @Override
    public void deleteBatch(List<Long> ids) {
        if(ids.isEmpty() || ids.size() == 0){
            return;
        }
        setmealMapper.deleteBatch(ids);
    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    @Override
    public void update(SetmealDTO setmealDTO) {
        //修改套餐基础信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);
        //修改套餐跟菜品之间的关系
        Long mealId = setmealDTO.getId();
        //1 全删除
        setMealDishMapper.deleteByMealId(mealId);
        //2 再重新建立关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if(setmealDishes.isEmpty() || setmealDishes.size() == 0){
            return;
        }
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(mealId);
            Long dishId = dishMapper.getDishIdByName(setmealDish.getName());
            setmealDish.setDishId(dishId);
        }
        setMealDishMapper.insertBatch(setmealDishes);
    }

    @Override
    public SetmealDTO getById(Long id) {
        Setmeal setmeal = setmealMapper.getById(id);
        List<SetmealDish> list  = setMealDishMapper.getListByMealId(id);
        SetmealDTO setmealDTO = new SetmealDTO();
        BeanUtils.copyProperties(setmeal,setmealDTO);
        setmealDTO.setSetmealDishes(list);
        return setmealDTO;
    }
}
