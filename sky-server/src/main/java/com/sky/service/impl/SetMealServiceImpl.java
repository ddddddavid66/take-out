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

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<Setmeal> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        Long total = page.getTotal();
        List<Setmeal> list = page.getResult();
        return new PageResult(total,list);
    }
}
