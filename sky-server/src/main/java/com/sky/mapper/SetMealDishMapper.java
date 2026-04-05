package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetMealDishMapper {

    List<Long> getSetMealIdByDishId(List<Long> ids);


    void insertBatch(List<SetmealDish> setMealDishes);

    @Delete("delete from setmeal_dish where setmeal_id = #{mealId}")
    void deleteByMealId(Long mealId);

    @Select("select * from setmeal_dish where setmeal_id = #{mealId}")
    List<SetmealDish> getListByMealId(Long mealId);

}
