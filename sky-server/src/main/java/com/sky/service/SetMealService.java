package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SetMealService {

    void saveWithDish(SetmealDTO setmealDTO);

    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    void deleteBatch(List<Long> ids);

    void update(SetmealDTO setmealDTO);

    SetmealDTO getById(Long id);

    void startOrStop(Integer status, Long id);
}
