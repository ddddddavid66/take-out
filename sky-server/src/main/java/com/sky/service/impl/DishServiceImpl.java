package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetMealDishMapper setMealDishMapper;


    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //插入菜品表 1个
        dishMapper.insert(dish);
        //前端传不了 id
        Long dishId = dish.getId();
        //插入口味表 m个
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors.isEmpty() || flavors.size() == 0) {
            return;
        }
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishId);
        }
        dishFlavorMapper.insertBatch(flavors); //批量插入
    }

    /**
     * 菜品分页查询
     *
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }


    /**
     * 菜品批量删除
     *
     * @param ids
     */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        for (Long id : ids) {
            //判断是否起售
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //判断是否关联套餐 套餐表
        List<Long> setMealIds = setMealDishMapper.getSetMealIdByDishId(ids);
        if (setMealIds != null && setMealIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除
        dishMapper.deleteByIds(ids);
        //还要删除 口味表
        dishFlavorMapper.deleteByDishIds(ids);
    }

    @Override
    public DishVO getById(Long id) {
        //根据id查询菜品
        Dish dish = dishMapper.getById(id);
        //查询口味
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
        //创建VO对象
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    @Override
    public void updateWithFlavors(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

//        更改菜品信息
        dishMapper.update(dish);

//        删除口味信息，并添加新的口味
        ArrayList<Long> dishIds = new ArrayList<>();
        dishIds.add(dish.getId());
        dishFlavorMapper.deleteByDishIds(dishIds);
//        添加新的口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        flavors.forEach(flavor -> {flavor.setDishId(dish.getId());});
        dishFlavorMapper.insertBatch(flavors);
    }

    @Override
    public List<Dish> getByCategotyId(Integer categoryId) {
        List<Dish> dishList =  dishMapper.getByCategotyId(categoryId);
        return dishList;
    }
}