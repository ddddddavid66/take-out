package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetMealMapper setMealMapper;

    @Override
    @Transactional
    public void addshoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //判断 商品 是否存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        //获取userid就是拦截器配合ThreadLocal获取
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if(list != null && list.size() > 0){ //已经存在 需要数量加1
            ShoppingCart cart = list.get(0); //只能有一个 因为一个userId对应一个购物车
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cart);
            return;
        }
        //不存在 直接插入 shoppingCart
        //我们还需要 设置 金额 图片 名称 什么的
        //判断商品是菜品还是套餐
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();
        if(dishId != null){ //添加购物车为菜品
            Dish dish = dishMapper.getById(dishId);
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());
        }else{ //是套餐
            Setmeal setMeal = setMealMapper.getById(setmealId);
            shoppingCart.setName(setMeal.getName());
            shoppingCart.setImage(setMeal.getImage());
            shoppingCart.setAmount(setMeal.getPrice());
        }
        //共有项
        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());
        //insert
        shoppingCartMapper.insert(shoppingCart);
    }
}
