package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {

     PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    public void saveWithFlavor(DishDTO dishDTO);

    void deleteBatch(List<Long> ids);

    DishVO getById(Long id);

    void updateWithFlavors(DishDTO dishDTO);
}
