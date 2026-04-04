package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.service.SetMealService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/setmeal")
@Slf4j
@Api(tags = "套餐相关接口")
public class SetMealController {
    @Autowired
    private SetMealService setMealService;
    @Autowired
    private DishService dishService;
    /**
     * 设置套餐
     * @return
     */
    @PostMapping
    @ApiOperation("创建套餐")
    public Result save(@RequestBody SetmealDTO setmealDTO){
        log.info("正在创建套餐{}",setmealDTO);
        setMealService.saveWithDish(setmealDTO);
        return Result.success();
    }

    @GetMapping("page")
    @ApiOperation("套餐分页查询实现")
    public Result pageQuery(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("正在执行套餐分页查询{}",setmealPageQueryDTO);
        PageResult pageResult = setMealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }


    @DeleteMapping
    @ApiOperation("删除套餐")
    public Result deleteBatch(@RequestParam List<Long> ids){
        log.info("正在删除id为{}的套餐",ids);
        setMealService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealDTO> getById(@PathVariable Long id){
        log.info("正在根据id{}查询套餐",id);
        SetmealDTO setmealDTO = setMealService.getById(id);
        return Result.success(setmealDTO);
    }

    @PutMapping
    @ApiOperation("修改套餐")
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("正在修改套餐为{}",setmealDTO);
        setMealService.update(setmealDTO);
        return Result.success();
    }
}
